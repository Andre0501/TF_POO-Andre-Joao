#!/usr/bin/env python3
"""Teste de integração destrutivo APENAS para a demonstração da interface.
Uso: python3 extra-web/tests/verificar_api.py http://localhost:8080 --demo
--demo autoriza substituir os dados da central visual pelos exemplos.
--manter deixa o cenário final para conferir a retomada em outro processo.
"""
import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

if '--demo' not in sys.argv:
    raise SystemExit('Use --demo em uma central de demonstração. O teste reinicia os dados da interface.')
BASE = next((x.rstrip('/') for x in sys.argv[1:] if x.startswith('http')), 'http://localhost:8080')
token = None
checks = []

def request(path, data=None, expected=200, with_token=True, method=None):
    headers = {}
    body = None
    if data is not None:
        body = urllib.parse.urlencode(data).encode()
        headers['Content-Type'] = 'application/x-www-form-urlencoded'
        if with_token:
            headers['X-Resgate-Token'] = token
    req = urllib.request.Request(BASE+path, data=body, headers=headers, method=method)
    try:
        response = urllib.request.urlopen(req, timeout=15)
    except urllib.error.HTTPError as error:
        response = error
    status = response.code
    raw = response.read()
    assert status == expected, (path, status, expected, raw[:300])
    if 'application/json' in response.headers.get('Content-Type', ''):
        return json.loads(raw)
    return raw.decode('utf-8')

def state():
    global token
    result = request('/api/estado')
    token = result['token']
    return result

def command(action, expected=200, **params):
    return request('/api/acao', dict(acao=action, **params), expected)

def check(name, condition):
    if not condition:
        raise AssertionError(name)
    checks.append(name)
    print('[OK]', name)

def robot(s, id):
    return next(r for r in s['robos'] if r['id'] == id)

def mission(s, id):
    return next(m for m in s['missoes'] if m['id'] == id)

def reset():
    return command('reiniciarDemo', confirmacao='REINICIAR')

state()
reset()
try:
    s = state()
    check('Dados iniciais reais: 4 robôs, 3 áreas, 4 missões',
          [len(s[k]) for k in ['robos','areas','missoes']] == [4,3,4])
    check('Fila inicial FIFO', s['fila'] == ['M1','M2','M3','M4'])
    check('HTML servido pelo Java', '<title>Resgate' in request('/'))
    check('CSS e JavaScript disponíveis', ':root' in request('/app.css') and 'function render()' in request('/app.js'))
    request('/arquivo-inexistente', expected=404)
    check('Rotas estáticas limitadas', True)
    request('/api/acao', {'acao':'salvar'}, expected=403, with_token=False)
    check('Comando sem token é recusado', state()['salvoEm'] == s['salvoEm'])
    c = request('/api/candidatos?id=M3')
    check('Sugestão M3 vem da central Java', c['sugestao'] == 'T1')
    check('Consumos reais comparados', [(r['robo'],r['consumo']) for r in c['candidatos'] if r['apto']] == [('T1',20),('A1',25)])
    command('atribuir', 400, robo='T1', missao='M4')
    check('Incompatibilidade recusada sem alterar o estado', state()['salvoEm'] == s['salvoEm'])
    s = command('atribuir', robo='D1', missao='M1')
    check('Reserva afeta missão e robô', mission(s,'M1')['robo'] == 'D1' and robot(s,'D1')['status'] == 'RESERVADO')
    check('Reserva mantém fila e bateria', s['fila'][0] == 'M1' and robot(s,'D1')['bateria'] == 100)
    command('atribuir',400,robo='D1',missao='M2')
    check('Segunda atribuição é bloqueada', mission(state(),'M2')['robo'] is None)
    s = command('cancelar', missao='M1')
    check('Cancelamento antes do início não consome', robot(s,'D1')['bateria'] == 100 and robot(s,'D1')['missao'] is None)
    reset()
    s = command('atenderProxima')
    check('Atendimento automático reserva a primeira sem executar', mission(s,'M1')['robo']=='D1' and mission(s,'M1')['status']=='PENDENTE')
    s = command('iniciar', missao='M1')
    check('Início atualiza fila, missão e robô', s['fila']==['M2','M3','M4'] and mission(s,'M1')['status']=='EM_ANDAMENTO' and robot(s,'D1')['status']=='EM_MISSAO')
    check('Previsão de 25 pontos fixada no início', mission(s,'M1')['previsto']==25 and robot(s,'D1')['bateria']==100)
    command('atualizarRisco', area='AR2', risco='BAIXO')
    s = command('executar', missao='M1')
    check('Risco depois do início preserva o custo fixado', mission(s,'M1')['aplicado']==25 and robot(s,'D1')['bateria']==75)
    check('Conclusão libera vínculo e conta sucesso', robot(s,'D1')['missao'] is None and robot(s,'D1')['realizadas']==1 and mission(s,'M1')['status']=='CONCLUIDA')
    command('iniciar',400,missao='M1')
    check('Reinício de missão concluída é recusado', mission(state(),'M1')['status']=='CONCLUIDA')
    text = 'Instrução "área segura" & <teste>\nAguardar confirmação.'
    s = command('enviarInstrucao',robo='D1',mensagem=text)
    check('Acentos, aspas e quebra de linha preservados no JSON', text in robot(s,'D1')['mensagens'][-1]['texto'])
    s = command('receberInformacao',robo='D1',mensagem='Unidade pronta para o próximo chamado.')
    check('Resposta registrada no sentido robô → central', 'ROBO D1 -> CENTRAL' in robot(s,'D1')['mensagens'][-1]['texto'])
    s = command('recarregar',robo='D1')
    check('Recarga persiste 100%',robot(s,'D1')['bateria']==100)
    s = command('iniciarManutencao',robo='D1')
    check('Manutenção retira a disponibilidade',robot(s,'D1')['status']=='EM_MANUTENCAO')
    s = command('recarregar',robo='D1')
    check('Recarga não encerra manutenção',robot(s,'D1')['status']=='EM_MANUTENCAO')
    s = command('finalizarManutencao',robo='D1')
    check('Finalização de manutenção libera a unidade',robot(s,'D1')['status']=='DISPONIVEL')
    command('atribuir',robo='A1',missao='M4')
    s = command('executar',missao='M4')
    check('Execução direta de pendente atribuída inicia e conclui',mission(s,'M4')['status']=='CONCLUIDA' and robot(s,'A1')['bateria']==60)
    command('atribuir',robo='T1',missao='M3')
    command('iniciar',missao='M3')
    s = command('cancelar',missao='M3')
    check('Cancelamento iniciado aplica custo sem contar sucesso',robot(s,'T1')['bateria']==80 and robot(s,'T1')['realizadas']==0 and mission(s,'M3')['aplicado']==20)
    area_name = 'Área "Água" & Sol <setor>'
    s = command('cadastrarArea',id='AR4',nome=area_name,localizacao='Setor 4',terreno='URBANO',risco='MEDIO',
                descricao='Área fictícia <img src=x onerror="window.__xss=1">.')
    check('Cadastro de área preserva os textos sem interpretá-los',next(a for a in s['areas'] if a['id']=='AR4')['nome']==area_name)
    s = command('cadastrarRobo',id='D3',nome='Explorador',tipo='DRONE',bateria='10',capacidade='120')
    check('Novo robô vem da subclasse Java',robot(s,'D3')['tipo']=='DRONE' and robot(s,'D3')['capacidade']==120)
    command('cadastrarRobo',400,id='D3',nome='Duplicado',tipo='DRONE',bateria='50',capacidade='10')
    check('ID repetido é recusado',len(state()['robos'])==5)
    command('cadastrarRobo',400,id='X',nome='Inválido',tipo='DRONE',bateria='1.5',capacidade='10')
    check('Número não inteiro é recusado',len(state()['robos'])==5)
    s = command('registrarMissao',id='M5',descricao='Reconhecer setor de teste',tipo='RECONHECIMENTO',area='AR4',prioridade='ALTA',peso='0')
    check('Nova missão entra no fim da fila',s['fila']==['M2','M5'] and mission(s,'M5')['area']=='AR4')
    candidates = request('/api/candidatos?id=M5')['candidatos']
    check('Unidade compatível com bateria insuficiente é identificada',not next(r for r in candidates if r['robo']=='D3')['apto'])
    command('atribuir',400,robo='D3',missao='M5')
    check('Validação Java impede reserva sem bateria',robot(state(),'D3')['missao'] is None)
    command('registrarMissao',400,id='M6',descricao='Carga inválida',tipo='RECONHECIMENTO',area='AR4',prioridade='ALTA',peso='10')
    check('Missão inválida não entra no mapa nem na fila',len(state()['missoes'])==5 and 'M6' not in state()['fila'])
    report = request('/api/relatorio')
    check('Relatório produzido pela classe original', 'RELATORIO GERAL DE RESGATE' in report['texto'] and 'Concluidas: 2' in report['texto'])
    check('Download TXT mantém o conteúdo da central','Explorador' in request('/api/relatorio.txt'))
    s = command('salvar')
    check('Salvamento explícito preserva as métricas',s['metricas']['concluidas']==2 and s['metricas']['canceladas']==1)
    expected={k:v for k,v in state().items() if k not in ('token','salvoEm','mensagem')}
    folder=Path(__file__).resolve().parent
    (folder/'estado_esperado_retomada.json').write_text(json.dumps(expected,ensure_ascii=False,indent=2),encoding='utf-8')
    (folder/'verificacao_api.json').write_text(json.dumps({'aprovados':len(checks),'verificacoes':checks},ensure_ascii=False,indent=2),encoding='utf-8')
    print('\nRESULTADO:',len(checks),'verificações de integração aprovadas.')
finally:
    if '--manter' not in sys.argv:
        reset()
