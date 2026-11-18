'use strict';

// Interface extra: nenhuma regra de missão é executada no navegador.
// Todas as alterações são confirmadas pelo servidor Java antes de atualizar a tela.
const $ = (selector, root = document) => root.querySelector(selector);
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const icons = {
  dashboard:'<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="11" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="18" width="7" height="3" rx="1"/>',
  missions:'<rect x="5" y="5" width="14" height="16" rx="2"/><path d="M9 5V3h6v2M9 10h6M9 14h6M9 18h4"/>',
  robot:'<rect x="5" y="7" width="14" height="12" rx="3"/><path d="M12 3v4M3 11v4m18-4v4M9 19v2m6-2v2M9 15h6"/><circle cx="9" cy="11" r=".6"/><circle cx="15" cy="11" r=".6"/>',
  map:'<path d="m3 6 6-3 6 3 6-3v15l-6 3-6-3-6 3zM9 3v15m6-12v15"/>',
  chart:'<path d="M4 3v17h17M8 15v-4m5 4V7m5 8V4"/>',
  activity:'<path d="M3 12h4l3-8 4 16 3-8h4"/>',
  help:'<circle cx="12" cy="12" r="9"/><path d="M9.5 9a2.5 2.5 0 0 1 5 .3c0 2-2.5 2-2.5 4M12 17h.01"/>',
  arrowUpRight:'<path d="M6 18 18 6M6 6h12v12"/>',
  arrow:'<path d="M5 12h14m-6-6 6 6-6 6"/>',
  chevron:'<path d="m9 5 7 7-7 7"/>',
  plus:'<path d="M12 5v14M5 12h14"/>',
  search:'<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>',
  moon:'<path d="M20.8 13a9 9 0 0 1-9.8-9.8A9 9 0 1 0 20.8 13Z"/>',
  sun:'<circle cx="12" cy="12" r="4"/><path d="M12 2v2m0 16v2M2 12h2m16 0h2M5 5l1.5 1.5m11 11L19 19M5 19l1.5-1.5m11-11L19 5"/>',
  menu:'<path d="M4 6h16M4 12h16M4 18h16"/>',
  save:'<path d="M5 3h12l4 4v14H3V3h2zM7 3v6h9V3M7 21v-7h10v7"/>',
  clock:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  check:'<path d="m5 12 4 4L19 6"/>',
  checkCircle:'<circle cx="12" cy="12" r="9"/><path d="m7.5 12 3 3 6-6"/>',
  close:'<path d="m6 6 12 12M18 6 6 18"/>',
  warning:'<path d="m12 3 10 18H2zM12 9v5m0 3h.01"/>',
  bolt:'<path d="m13 2-9 12h7l-1 8 10-12h-7z"/>',
  tool:'<path d="M14 6a5 5 0 0 1-6 6l-5 5a3 3 0 0 0 4 4l5-5a5 5 0 0 0 6-6l-4 4-4-4 4-4z"/>',
  pin:'<path d="M19 10c0 5-7 11-7 11S5 15 5 10a7 7 0 0 1 14 0z"/><circle cx="12" cy="10" r="2"/>',
  scan:'<path d="M8 3H3v5m13-5h5v5M3 16v5h5m13-5v5h-5"/><circle cx="12" cy="12" r="4"/><path d="M12 6v2m0 8v2m-6-6h2m8 0h2"/>',
  people:'<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3m1-16a3 3 0 0 1 0 6m2 4a5 5 0 0 1 3 4v2"/>',
  cargo:'<path d="m12 3 9 5v9l-9 5-9-5V8zM3 8l9 5 9-5M12 13v9M7.5 5.5l9 5"/>',
  drone:'<rect x="8" y="8" width="8" height="8" rx="2"/><path d="m8 8-3-3m11 3 3-3M8 16l-3 3m11-3 3 3"/><circle cx="4" cy="4" r="3"/><circle cx="20" cy="4" r="3"/><circle cx="4" cy="20" r="3"/><circle cx="20" cy="20" r="3"/>',
  waves:'<path d="M2 7c3-4 5 4 8 0s5 4 8 0 4 0 4 0M2 13c3-4 5 4 8 0s5 4 8 0 4 0 4 0M2 19c3-4 5 4 8 0s5 4 8 0 4 0 4 0"/>',
  mountain:'<path d="m2 20 7-14 4 7 3-9 6 16zM6 12l3 2 2-2m3-3 2 2 2-2"/>',
  building:'<path d="M4 21V7h8v14m0-18h8v18M2 21h20M7 11h2m-2 4h2m6-8h2m-2 4h2m-2 4h2"/>',
  download:'<path d="M12 3v12m-5-5 5 5 5-5M4 16v5h16v-5"/>',
  print:'<path d="M6 9V3h12v6M6 18H3V9h18v9h-3M6 14h12v7H6zM17 11h.01"/>',
  refresh:'<path d="M21 3v6h-6M3 21v-6h6M5.5 6a8 8 0 0 1 13.8 1M4.7 17a8 8 0 0 0 13.8 1"/>',
  play:'<path d="m8 4 12 8-12 8z"/>',
  send:'<path d="m22 2-7 20-4-9-9-4zM22 2 11 13"/>',
  wifiOff:'<path d="m3 3 18 18M2 8a16 16 0 0 1 6-3m5 0a16 16 0 0 1 9 3M5 12a11 11 0 0 1 6-3m7 3h1M8 16a6 6 0 0 1 5-2m-1 6h.01"/>',
  info:'<circle cx="12" cy="12" r="9"/><path d="M12 11v6m0-10h.01"/>',
};
function icon(name) { return `<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${icons[name] || icons.robot}</svg>`; }
function hydrate(root = document) { root.querySelectorAll('[data-icon]').forEach(el => el.innerHTML = icon(el.dataset.icon)); }
const labels = {
  DRONE:'Drone', TERRESTRE:'Terrestre', ANFIBIO:'Anfíbio',
  DISPONIVEL:'Disponível', RESERVADO:'Reservado', EM_MISSAO:'Em missão', EM_MANUTENCAO:'Manutenção', SEM_BATERIA:'Sem bateria',
  PENDENTE:'Pendente', EM_ANDAMENTO:'Em andamento', CONCLUIDA:'Concluída', CANCELADA:'Cancelada',
  RECONHECIMENTO:'Reconhecimento', LOCALIZACAO_PESSOAS:'Localização de pessoas', TRANSPORTE_SUPRIMENTOS:'Transporte de suprimentos',
  URBANO:'Urbano', IRREGULAR:'Irregular', ALAGADO:'Alagado', BAIXO:'Baixo', MEDIO:'Médio', ALTO:'Alto',
  BAIXA:'Baixa', MEDIA:'Média', ALTA:'Alta',
};
const pages = {
  inicio:['Central de operações','Tudo o que a sua equipe precisa para agir.','Visão geral'],
  missoes:['Missões de resgate','Da primeira atribuição à conclusão. Acompanhe cada etapa.','Missões'],
  frota:['Sua frota, pronta para agir','Capacidades, energia e disponibilidade em um só lugar.','Frota de robôs'],
  areas:['Áreas de operação','Conheça o terreno. Antecipe os desafios de cada missão.','Áreas de operação'],
  relatorios:['O panorama da operação','Resultados e indicadores reais da sua central.','Relatórios'],
  atividade:['Cada ação deixa um registro','A linha do tempo das decisões e operações da central.','Atividade'],
};
let state = null;
let page = pages[location.hash.slice(1)] ? location.hash.slice(1) : 'inicio';
let connected = false;
let busy = false;
let revision = 0;
let detail = null;
let detailRequest = 0;
let reportRequest = 0;
let lastActiveElement = null;
const filters = {missoes:{q:'',status:'',tipo:''},frota:{q:'',status:'',tipo:''},areas:{q:'',risco:'',terreno:''},atividade:{q:'',tipo:''}};
const label = key => labels[key] || key;
const areaOf = m => state.areas.find(a => a.id === m.area);
const roboOf = m => state.robos.find(r => r.id === m.robo);
const activeIn = id => state.missoes.filter(m => m.area === id && ['PENDENTE','EM_ANDAMENTO'].includes(m.status));
const norm = value => String(value ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();
const matches = (q, ...values) => norm(values.join(' ')).includes(norm(q));
const plural = (n,one,many) => `${n} ${n===1?one:many}`;
const fmt = (value, digits = 0) => new Intl.NumberFormat('pt-BR',{maximumFractionDigits:digits}).format(value || 0);
const when = value => value ? new Intl.DateTimeFormat('pt-BR',{timeZone:'America/Sao_Paulo',day:'2-digit',month:'short',hour:'2-digit',minute:'2-digit'}).format(new Date(value)) : 'Horário indisponível';
const time = value => value ? new Intl.DateTimeFormat('pt-BR',{timeZone:'America/Sao_Paulo',hour:'2-digit',minute:'2-digit'}).format(new Date(value)) : '—';
const day = value => value ? new Intl.DateTimeFormat('pt-BR',{timeZone:'America/Sao_Paulo',day:'numeric',month:'long',year:'numeric'}).format(new Date(value)) : 'Registros';
function ago(value) {
  if (!value) return 'Registro da central';
  const minutes = Math.max(0, Math.floor((Date.now() - new Date(value).getTime())/60000));
  return minutes < 1 ? 'Agora' : minutes < 60 ? `Há ${minutes} min` : when(value);
}
function button(text, ico, attrs = '', classes = '') {
  return `<button type="button" class="button ${classes}" ${attrs}>${ico ? icon(ico) : ''}${text}</button>`;
}
function status(key, text = label(key)) {
  const color = ['DISPONIVEL','CONCLUIDA','BAIXO'].includes(key) ? 'green' : ['EM_MISSAO','EM_ANDAMENTO'].includes(key) ? 'blue' : ['RESERVADO','PENDENTE','MEDIO','EM_MANUTENCAO'].includes(key) ? 'amber' : ['CANCELADA','SEM_BATERIA','ALTO'].includes(key) ? 'red' : 'gray';
  return `<span class="status ${color}">${esc(text)}</span>`;
}
const missionStatus = m => m.status === 'PENDENTE' ? status(m.robo ? 'RESERVADO' : 'PENDENTE',m.robo ? 'Atribuída' : 'Na fila') : status(m.status);
const missionIcon = tipo => tipo === 'RECONHECIMENTO' ? 'scan' : tipo === 'LOCALIZACAO_PESSOAS' ? 'people' : 'cargo';
const robotIcon = tipo => tipo === 'DRONE' ? 'drone' : tipo === 'ANFIBIO' ? 'waves' : 'robot';
const terrainIcon = terreno => terreno === 'URBANO' ? 'building' : terreno === 'ALAGADO' ? 'waves' : 'mountain';
function priority(p) { return `<span class="priority ${p.toLowerCase()}"><i aria-hidden="true"><b></b><b></b><b></b></i>${label(p)}</span>`; }
function battery(value) { return `<div class="battery ${value <= 20 ? 'low' : value <= 40 ? 'medium' : ''}" role="meter" aria-label="Bateria" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${value}"><span style="width:${value}%"></span></div>`; }
function empty(title, description, action = '', compact = false) {
  return `<div class="empty-state ${compact ? 'compact' : ''}">${icon('scan')}<h3>${esc(title)}</h3><p>${esc(description)}</p>${action}</div>`;
}
function stat(title, value, subtitle, ico, tone = '') {
  return `<article class="card stat-card"><div class="stat-top"><span>${title}</span><span class="stat-icon">${icon(ico)}</span></div><div class="stat-number">${value}</div><div class="stat-bottom"><i class="tiny-dot ${tone}"></i>${subtitle}</div></article>`;
}
function terrainArt(terrain) {
  const art = terrain === 'URBANO'
    ? '<path d="m15 76 49-26 49 26-49 25Z" fill="currentColor" opacity=".08"/><path d="m29 68 21-11 21 11-21 11Z" fill="currentColor" opacity=".15"/><path d="M29 68V38l21-11 21 11v30L50 79Z" fill="currentColor" opacity=".18"/><path d="M29 38 50 49l21-11M50 49v30M36 45v7m0 5v7m8-15v7m13-6v7m7-12v7M77 73V49l14-8 15 8v24L91 81Z" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m77 49 14 8 15-8M91 57v24M29 38 50 27l21 11v30L50 79 29 68Z" fill="none" stroke="currentColor" stroke-width="1.7"/>'
    : terrain === 'ALAGADO'
    ? '<path d="M13 72c12-12 16 12 28 0s16 12 28 0 16 12 28 0 16 12 28 0M13 84c12-12 16 12 28 0s16 12 28 0 16 12 28 0 16 12 28 0" fill="none" stroke="currentColor" stroke-width="2" opacity=".5"/><path d="m27 61 16-25 15 25ZM74 62l14-31 19 31Z" fill="currentColor" opacity=".12"/><path d="M44 63V31m-12 8 12-20 12 20-12-4-12 4Zm56 23V26m-11 9 11-20 12 20-12-4-11 4Z" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m60 48 12 3-9 9-10-3Z" fill="currentColor" opacity=".2"/>'
    : '<path d="m8 78 39-54 25 34 15-22 36 42Z" fill="currentColor" opacity=".12"/><path d="m8 78 39-54 25 34 15-22 36 42M33 43l14 8 10-13m20 14 10 6 7-11M48 78l17-9 8 9m12-1 10-8 7 9" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="m19 86 92 1M7 92h61m10 0h45" fill="none" stroke="currentColor" stroke-width="1.4" opacity=".4"/>';
  return `<svg viewBox="0 0 135 106" fill="none" aria-hidden="true">${art}</svg>`;
}
function robotArt(tipo) {
  const art = tipo === 'DRONE'
    ? '<path d="m64 44-27-14m79 14 27-14M64 64 37 80m79-16 27 16" stroke="currentColor" stroke-width="7"/><ellipse cx="31" cy="25" rx="24" ry="8" fill="currentColor" opacity=".08"/><ellipse cx="149" cy="25" rx="24" ry="8" fill="currentColor" opacity=".08"/><ellipse cx="31" cy="84" rx="24" ry="8" fill="currentColor" opacity=".08"/><ellipse cx="149" cy="84" rx="24" ry="8" fill="currentColor" opacity=".08"/><path d="M8 25h46m72 0h46M8 84h46m72 0h46" stroke="currentColor" stroke-width="2.3" stroke-linecap="round"/><path d="M31 18v14m118-14v14M31 77v14m118-14v14" stroke="currentColor" stroke-width="2"/><rect x="62" y="35" width="56" height="38" rx="13" fill="currentColor" opacity=".18"/><rect x="65" y="37" width="50" height="31" rx="10" stroke="currentColor" stroke-width="2"/><path d="m78 73-5 13h34l-5-13" stroke="currentColor" stroke-width="2"/><rect x="82" y="45" width="16" height="6" rx="3" fill="#ee9a68"/><circle cx="90" cy="63" r="5" fill="currentColor"/>'
    : tipo === 'ANFIBIO'
    ? '<path d="M20 78c8-8 14 8 22 0s14 8 22 0 14 8 22 0 14 8 22 0 14 8 22 0 14 8 22 0M30 91c8-8 14 8 22 0s14 8 22 0 14 8 22 0 14 8 22 0 14 8 22 0" stroke="currentColor" opacity=".35" stroke-width="2"/><path d="m32 57 14 20h87l17-20Z" fill="currentColor" opacity=".2"/><path d="m32 57 14 20h87l17-20ZM58 57V34h48l19 23M67 34V22h19v12" stroke="currentColor" stroke-width="2"/><path d="M72 41h20v9H72Z" fill="currentColor" opacity=".3"/><path d="M108 42h6l9 9h-15Z" fill="#ee9a68"/><path d="M41 65h96M78 22v-9" stroke="currentColor" stroke-width="2"/><circle cx="78" cy="11" r="3" fill="#ee9a68"/>'
    : '<rect x="32" y="58" width="116" height="27" rx="13" fill="currentColor" opacity=".22"/><rect x="32" y="58" width="116" height="27" rx="13" stroke="currentColor" stroke-width="2"/><circle cx="49" cy="72" r="8" stroke="currentColor" stroke-width="2"/><circle cx="76" cy="72" r="8" stroke="currentColor" stroke-width="2"/><circle cx="103" cy="72" r="8" stroke="currentColor" stroke-width="2"/><circle cx="131" cy="72" r="8" stroke="currentColor" stroke-width="2"/><path d="M50 58V36h78v22M60 36V25h28v11M74 25V13" stroke="currentColor" stroke-width="2"/><rect x="63" y="40" width="19" height="10" rx="2" fill="currentColor" opacity=".3"/><rect x="90" y="40" width="23" height="10" rx="2" fill="#ee9a68" opacity=".8"/><circle cx="74" cy="11" r="3" fill="#ee9a68"/>';
  return `<svg viewBox="0 0 180 110" fill="none" role="img" aria-label="Ilustração: ${label(tipo)}">${art}</svg>`;
}

async function api(path, options = {}) {
  const response = await fetch(path, {cache:'no-store',...options});
  let data;
  try { data = await response.json(); } catch { throw new Error('A central não retornou uma resposta válida. Confira se o servidor está aberto.'); }
  if (!response.ok) {
    const error = new Error(data.erro || 'Não foi possível realizar a operação.');
    error.status = response.status;
    throw error;
  }
  return data;
}
function setConnection(ok) {
  connected = ok;
  $('#connectionBanner').hidden = ok;
  $('#connectionDot').classList.toggle('offline',!ok);
  $('#connectionLabel').textContent = ok ? 'Central conectada' : 'Central desconectada';
  document.querySelectorAll('[data-command], [data-new], [data-submit]').forEach(b => b.disabled = !ok || busy || b.dataset.blocked === 'true');
}
async function loadState(initial = false) {
  const currentRevision = revision;
  try {
    const data = await api('/api/estado');
    if (busy || currentRevision !== revision) return;
    const changed = !state || data.salvoEm !== state.salvoEm;
    state = data;
    setConnection(true);
    if (changed || initial) {
      render();
      if (detail) refreshDetail();
    }
    updateFooter();
  } catch (error) {
    setConnection(false);
    if (!state) $('#pageContent').innerHTML = empty('Não foi possível conectar','Inicie o servidor pelo arquivo iniciar-web e abra o endereço informado no terminal.',button('Tentar novamente','refresh','data-retry'));
  }
}
function updateFooter() {
  if (state) $('#savedLabel').textContent = `Salvo automaticamente às ${time(state.salvoEm)}`;
}
function toast(message, error = false) {
  const el = document.createElement('div');
  el.className = `toast${error ? ' error' : ''}`;
  el.setAttribute('role',error ? 'alert' : 'status');
  el.innerHTML = `${icon(error ? 'warning' : 'checkCircle')}<span>${esc(message)}</span><button class="icon-button" aria-label="Fechar aviso">${icon('close')}</button>`;
  $('button',el).addEventListener('click',()=>el.remove());
  const active=Array.from(document.querySelectorAll('dialog[open]')).at(-1);
  let region=$('#toasts');
  if(active){
    region=$('.dialog-toasts',active);
    if(!region){
      region=document.createElement('div');
      region.className='toast-region dialog-toasts';
      region.setAttribute('aria-live','polite');
      active.append(region);
    }
  }
  region.append(el);
  setTimeout(()=>el.remove(),error ? 11000 : 6500);
}
async function mutate(params, options = {}) {
  if (busy || !connected) return false;
  busy = true;
  revision++;
  document.body.classList.add('is-busy');
  document.querySelectorAll('[data-submit]').forEach(b=>b.disabled=true);
  if (options.errorEl) options.errorEl.hidden = true;
  try {
    const data = await api('/api/acao',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8','X-Resgate-Token':state.token},body:new URLSearchParams(params)});
    state = data;
    busy = false;
    if (options.closeForm) $('#formDialog').close();
    if (options.closeDetail) { detail=null; $('#detailDialog').close(); }
    render();
    if (detail) refreshDetail();
    toast(data.mensagem);
    return true;
  } catch (error) {
    if (error instanceof TypeError) setConnection(false);
    if (options.errorEl) { options.errorEl.hidden = false; options.errorEl.textContent = error.message; }
    else toast(error.message,true);
    if (error.status === 403) setTimeout(()=>loadState(),100);
    return false;
  } finally {
    busy = false;
    document.body.classList.remove('is-busy');
    setConnection(connected);

  }
}

function render() {
  const info = pages[page];
  $('#pageTitle').textContent = info[0];
  $('#pageDescription').textContent = info[1];
  $('#breadcrumb').textContent = info[2];
  document.title = `${info[2]} — Resgate`;
  document.querySelectorAll('[data-page]').forEach(a=>{
    a.classList.toggle('active',a.dataset.page===page);
    if (a.dataset.page===page) a.setAttribute('aria-current','page'); else a.removeAttribute('aria-current');
  });
  if (!state) return;
  $('#navCount').textContent = state.metricas.pendentes;
  const save = button('Salvar agora','save','data-command="salvar"');
  $('#pageActions').innerHTML = page === 'inicio' ? save + button('Nova missão','plus','data-new="missao"','primary')
    : page === 'missoes' ? button('Atender próxima','bolt','data-command="atenderProxima"') + button('Nova missão','plus','data-new="missao"','primary')
    : page === 'frota' ? button('Adicionar robô','plus','data-new="robo"','primary')
    : page === 'areas' ? button('Nova área','plus','data-new="area"','primary')
    : page === 'relatorios' ? button('Imprimir','print','data-print') + `<a class="button primary" href="/api/relatorio.txt" download>${icon('download')}Baixar relatório</a>` : save;
  $('#pageContent').innerHTML = page === 'inicio' ? overview() : page === 'missoes' ? missionsPage() : page === 'frota' ? fleetPage() : page === 'areas' ? areasPage() : page === 'relatorios' ? reportsPage() : activityPage();
  if (page === 'relatorios') loadReport();
  setConnection(connected);
  updateFooter();
}
function overview() {
  const m = state.metricas;
  const queue = state.fila.map(id=>state.missoes.find(x=>x.id===id)).filter(Boolean);
  const occupied = state.robos.filter(r=>['RESERVADO','EM_MISSAO'].includes(r.status)).length;
  const unavailable = m.totalRobos - m.disponiveis - occupied;
  const portion = count => m.totalRobos ? count/m.totalRobos*100 : 0;
  return `
    <div class="stats-grid">
      ${stat('Robôs disponíveis',fmt(m.disponiveis),`de ${plural(m.totalRobos,'unidade','unidades')} na frota`,'robot')}
      ${stat('Missões pendentes',fmt(m.pendentes),'Na fila de operações','clock','orange')}
      ${stat('Em operação',fmt(m.emAndamento),'Missões em andamento','activity','blue')}
      ${stat('Missões concluídas',fmt(m.concluidas),'Resgates e entregas finalizados','checkCircle')}
    </div>
    <div class="hero-strip"><div><h2>${m.emAndamento ? `${m.emAndamento} ${m.emAndamento===1?'missão em campo.':'missões em campo.'} A central acompanha.` : 'Sua operação, sob controle.'}</h2><p>${m.pendentes ? `${m.pendentes} ${m.pendentes===1?'missão aguarda':'missões aguardam'} os próximos passos da equipe.` : 'Nenhuma missão pendente. Tudo pronto para um novo chamado.'}</p></div><span class="hero-status">${icon('checkCircle')}Estado sincronizado</span></div>
    <div class="overview-grid">
      <div class="overview-left">
        <section class="card"><div class="card-head"><div><div class="card-heading"><h2>Fila de operações</h2><span class="count-pill">${queue.length}</span></div><p>Missões na ordem em que foram registradas</p></div><a class="text-button" href="#missoes">Ver todas ${icon('arrow')}</a></div>
          ${queue.length ? missionTable(queue.slice(0,5),true) : empty('Fila livre','Cadastre uma missão para começar uma nova operação.',button('Criar missão','plus','data-new="missao"','compact'),true)}
          <div class="table-foot"><span>Uma atribuição reserva o robô até o início.</span>${button('Atender próxima','bolt','data-command="atenderProxima"','compact ghost')}</div>
        </section>
        <section class="card"><div class="card-head"><div><h2>Prontidão da frota</h2><p>Energia e disponibilidade de cada unidade</p></div><a class="text-button" href="#frota">Ver frota ${icon('arrow')}</a></div>
          <div class="availability-row"><div class="availability-top"><span class="muted small">Disponibilidade atual</span><strong>${fmt(portion(m.disponiveis))}%</strong></div><div class="availability-track"><span style="width:${portion(m.disponiveis)}%"></span><span class="occupied" style="width:${portion(occupied)}%"></span><span class="offline" style="width:${portion(unavailable)}%"></span></div><div class="legend"><span><i></i>${m.disponiveis} disponíveis</span><span><i class="occupied"></i>${occupied} ocupados</span><span><i class="offline"></i>${unavailable} indisponíveis</span></div></div>
          <div class="mini-fleet">${state.robos.slice(0,4).map(r=>`<article class="mini-robot"><button class="mini-robot-button" data-open-robo="${esc(r.id)}"><div class="mini-robot-head"><div class="robot-mini-icon">${icon(robotIcon(r.tipo))}</div><div><strong>${esc(r.id)}</strong><span>${esc(r.nome)}</span></div></div>${icon('chevron')}</button><div><div class="battery-caption"><span>${label(r.status)}</span><strong>${r.bateria}%</strong></div>${battery(r.bateria)}</div></article>`).join('')}</div>
        </section>
      </div>
      <div class="overview-right">
        <section class="card"><div class="card-head"><div><h2>Áreas monitoradas</h2><p>${plural(state.areas.length,'setor','setores')} · visão esquemática</p></div><a class="text-button" href="#areas" aria-label="Ver todas as áreas">${icon('arrowUpRight')}</a></div>
          ${state.areas.length ? `<div class="sector-board">${state.areas.slice(0,6).map(a=>`<button class="sector terrain-${a.terreno}" data-open-area="${esc(a.id)}"><span class="sector-id">${esc(a.id)}</span><span class="sector-risk ${a.risco}" aria-label="Risco ${label(a.risco)}"></span><div class="sector-art">${terrainArt(a.terreno)}</div><strong>${esc(a.nome)}</strong><span>${plural(activeIn(a.id).length,'missão ativa','missões ativas')}</span></button>`).join('')}</div>` : empty('Sem áreas cadastradas','Adicione uma área para receber missões.',button('Nova área','plus','data-new="area"','compact'),true)}
          <div class="section-bottom-note">${icon('info')}Os terrenos influenciam a escolha do robô.</div>
        </section>
        <section class="card"><div class="card-head"><div><h2>Últimas atividades</h2><p>O que acontece na sua central</p></div><a class="text-button" href="#atividade" aria-label="Ver todas as atividades">${icon('arrowUpRight')}</a></div><div class="activity-list">${state.historico.slice(-5).reverse().map(activityItem).join('') || empty('Tudo pronto','Os próximos eventos aparecerão aqui.','',true)}</div></section>
      </div>
    </div>`;
}
function missionTable(missions, compact = false) {
  return `<div class="table-wrap"><table><thead><tr><th>MISSÃO</th><th>ÁREA</th>${compact ? '' : '<th>PRIORIDADE</th><th>RESPONSÁVEL</th>'}<th>SITUAÇÃO</th><th><span class="sr-only">Detalhes</span></th></tr></thead><tbody>${missions.map(m=>{
    const a = areaOf(m), r = roboOf(m);
    return `<tr><td><div class="mission-cell"><span class="mission-glyph">${icon(missionIcon(m.tipo))}</span><div><button class="mission-title" data-open-mission="${esc(m.id)}">${esc(m.descricao)}</button><span class="mission-caption">${esc(m.id)} · ${label(m.tipo)}</span></div></div></td><td><div class="cell-stack"><strong>${esc(a?.nome || m.area)}</strong><span>${a ? label(a.terreno) : ''}</span></div></td>${compact ? '' : `<td>${priority(m.prioridade)}</td><td><div class="cell-stack"><strong>${r ? esc(r.id) : '—'}</strong><span>${r ? esc(r.nome) : 'Sem atribuição'}</span></div></td>`}<td>${missionStatus(m)}</td><td><button class="icon-button" data-open-mission="${esc(m.id)}" aria-label="Abrir missão ${esc(m.id)}">${icon('chevron')}</button></td></tr>`;
  }).join('')}</tbody></table></div>`;
}
function options(values, selected, all) { return `<option value="">${all}</option>${values.map(v=>`<option value="${v}" ${selected===v?'selected':''}>${label(v)}</option>`).join('')}`; }
function searchField(group, placeholder) {
  return `<label class="search-field">${icon('search')}<input type="search" data-filter-group="${group}" data-filter="q" value="${esc(filters[group].q)}" placeholder="${placeholder}" aria-label="${placeholder}"></label>`;
}
function selectFilter(group,key,values,all) {
  return `<select class="filter-select" data-filter-group="${group}" data-filter="${key}" aria-label="${all}">${options(values,filters[group][key],all)}</select>`;
}
function missionsPage() {
  return `<section class="card"><div class="toolbar">${searchField('missoes','Buscar missão ou área…')}<div class="toolbar-filters">${selectFilter('missoes','status',['PENDENTE','EM_ANDAMENTO','CONCLUIDA','CANCELADA'],'Todas as situações')}${selectFilter('missoes','tipo',['RECONHECIMENTO','LOCALIZACAO_PESSOAS','TRANSPORTE_SUPRIMENTOS'],'Todos os tipos')}</div></div><div id="missionResults">${missionResults()}</div></section>`;
}
function missionResults() {
  const f = filters.missoes;
  const items = state.missoes.filter(m=>(!f.status||m.status===f.status)&&(!f.tipo||m.tipo===f.tipo)&&matches(f.q,m.id,m.descricao,areaOf(m)?.nome,m.robo,label(m.tipo)));
  return (items.length ? missionTable(items) : empty('Nenhuma missão encontrada','Cadastre uma nova missão ou ajuste os filtros de busca.',button('Nova missão','plus','data-new="missao"','compact'))) + `<div class="table-foot"><span>${items.length} de ${state.missoes.length} missões</span><span>Prioridade é informativa · atendimento automático em FIFO</span></div>`;
}
function fleetPage() {
  return `<div class="toolbar standalone-toolbar">${searchField('frota','Buscar robô…')}<div class="toolbar-filters">${selectFilter('frota','tipo',['DRONE','TERRESTRE','ANFIBIO'],'Todos os tipos')}${selectFilter('frota','status',['DISPONIVEL','RESERVADO','EM_MISSAO','EM_MANUTENCAO','SEM_BATERIA'],'Todos os estados')}</div></div><div id="fleetResults">${fleetResults()}</div>`;
}
function fleetResults() {
  const f = filters.frota;
  const items = state.robos.filter(r=>(!f.tipo||r.tipo===f.tipo)&&(!f.status||r.status===f.status)&&matches(f.q,r.id,r.nome,label(r.tipo)));
  if (!items.length) return `<section class="card">${empty('Nenhum robô encontrado','Adicione uma unidade ou ajuste os filtros.',button('Adicionar robô','plus','data-new="robo"','compact'))}</section>`;
  return `<div class="fleet-grid">${items.map(r=>`<article class="card fleet-card"><div class="fleet-card-top"><span class="type-label">${label(r.tipo).toUpperCase()}</span>${status(r.status)}</div><div class="robot-stage"><span class="stage-id">UNIT / ${esc(r.id)}</span>${robotArt(r.tipo)}</div><div class="fleet-title"><h2>${esc(r.nome)}</h2><span class="mono">${esc(r.id)}</span></div><div class="battery-caption"><span>Bateria atual</span><strong>${r.bateria}%</strong></div>${battery(r.bateria)}<div class="fleet-facts"><div><span>${r.tipo==='DRONE'?'Altitude máxima':'Capacidade de carga'}</span><strong>${r.capacidade} ${r.tipo==='DRONE'?'m':'kg'}</strong></div><div><span>Missões concluídas</span><strong>${plural(r.realizadas,'operação','operações')}</strong></div></div><div class="fleet-card-actions">${button('Ver unidade','arrow',`data-open-robo="${esc(r.id)}"`,'compact')}<button class="icon-button" data-command="recarregar" data-robo="${esc(r.id)}" aria-label="Recarregar ${esc(r.id)}" data-blocked="${Boolean(r.missao)}" ${r.missao?'disabled':''} title="${r.missao?'Finalize a missão ou retire a reserva antes de recarregar':'Recarregar bateria'}">${icon('bolt')}</button></div></article>`).join('')}</div>`;
}
function areasPage() {
  return `<div class="toolbar standalone-toolbar">${searchField('areas','Buscar área ou localização…')}<div class="toolbar-filters">${selectFilter('areas','terreno',['URBANO','IRREGULAR','ALAGADO'],'Todos os terrenos')}${selectFilter('areas','risco',['BAIXO','MEDIO','ALTO'],'Todos os riscos')}</div></div><div id="areaResults">${areaResults()}</div>`;
}
function areaResults() {
  const f = filters.areas;
  const items = state.areas.filter(a=>(!f.risco||a.risco===f.risco)&&(!f.terreno||a.terreno===f.terreno)&&matches(f.q,a.id,a.nome,a.localizacao,a.descricao));
  if (!items.length) return `<section class="card">${empty('Nenhuma área encontrada','Cadastre uma área de operação ou ajuste os filtros.',button('Nova área','plus','data-new="area"','compact'))}</section>`;
  return `<div class="area-grid">${items.map(a=>`<article class="card area-card"><div class="area-landscape ${a.terreno}">${status(a.risco,`Risco ${label(a.risco).toLowerCase()}`)}<span class="area-id">${esc(a.id)}</span>${terrainArt(a.terreno)}</div><div class="area-card-body"><h2>${esc(a.nome)}</h2><div class="area-location">${icon('pin')}${esc(a.localizacao)} · ${label(a.terreno)}</div><p class="area-desc">${esc(a.descricao)}</p><div class="area-card-bottom"><span>${plural(activeIn(a.id).length,'missão ativa','missões ativas')}</span><button class="text-button" data-open-area="${esc(a.id)}">Ver área ${icon('arrow')}</button></div></div></article>`).join('')}</div>`;
}
function reportsPage() {
  const m=state.metricas,total=m.totalMissoes;
  const parts=[['Pendentes',m.pendentes,''],['Em andamento',m.emAndamento,'running'],['Concluídas',m.concluidas,'complete'],['Canceladas',m.canceladas,'cancelled']];
  return `<div class="stats-grid">${stat('Missões registradas',fmt(total),'Em todos os estados','missions')}${stat('Concluídas',fmt(m.concluidas),total?`${fmt(m.concluidas/total*100,1)}% das missões registradas`:'Nenhuma missão registrada','checkCircle')}${stat('Bateria média',`${fmt(m.mediaBateria,1)}%`,`Entre ${m.totalRobos} robôs da frota`,'bolt','orange')}${stat('Áreas cadastradas',fmt(state.areas.length),'Terrenos monitorados','map','blue')}</div><div class="reports-grid"><section class="card"><div class="card-head"><div><h2>Distribuição de missões</h2><p>Estado atual de todas as operações</p></div></div><div class="chart-content"><div class="stack-bar" role="img" aria-label="Distribuição de missões por estado">${parts.map(([n,count,c])=>`<span class="${c}" style="width:${total?count/total*100:0}%" title="${n}: ${count}"></span>`).join('')}</div><div class="chart-legend">${parts.map(([n,count,c])=>`<div><i class="swatch ${c}"></i><span>${n}</span><strong>${count}</strong></div>`).join('')}</div></div></section><section class="card"><div class="card-head"><div><h2>Energia da frota</h2><p>Bateria atual por unidade</p></div><span class="small muted">0–100%</span></div><div class="battery-chart">${state.robos.map(r=>`<div class="battery-chart-row"><span>${esc(r.id)} · ${label(r.tipo)}</span>${battery(r.bateria)}<span>${r.bateria}%</span></div>`).join('') || '<p class="small muted">Nenhum robô cadastrado.</p>'}</div></section></div><section class="card"><div class="card-head"><div><h2>Relatório completo</h2><p id="reportTime">Gerando retrato da central…</p></div><button class="icon-button" data-refresh-report aria-label="Atualizar relatório">${icon('refresh')}</button></div><pre class="report-pre" id="reportText">Preparando o relatório…</pre></section>`;
}
async function loadReport() {
  const request=++reportRequest;
  try {
    const r=await api('/api/relatorio');
    if (request!==reportRequest || page!=='relatorios' || !$('#reportText')) return;
    $('#reportText').textContent=r.texto;
    $('#reportTime').textContent=`Gerado em ${when(r.geradoEm)} · retrato deste instante`;
  } catch(error) {
    if($('#reportText')) $('#reportText').textContent=error.message;
  }
}
function eventType(h) {
  const text=norm(h.texto);
  return text.includes('instrucao')||text.includes('informacao')?'mensagens':text.includes('area')?'areas':text.includes('missao')||text.includes('atribuicao')||text.includes('reservado')?'missoes':'frota';
}
function activityItem(h) {
  const tipo=eventType(h),ico=tipo==='mensagens'?'send':tipo==='areas'?'map':tipo==='missoes'?'missions':'robot';
  return `<div class="activity-item"><span class="activity-icon">${icon(ico)}</span><div class="activity-text">${esc(h.texto)}<time datetime="${esc(h.data)}">${ago(h.data)}</time></div></div>`;
}
function activityPage() {
  const f=filters.atividade;
  return `<section class="card"><div class="toolbar">${searchField('atividade','Buscar no histórico…')}<select class="filter-select" data-filter-group="atividade" data-filter="tipo" aria-label="Categoria de atividade"><option value="">Todas as atividades</option>${[['missoes','Missões'],['frota','Frota'],['areas','Áreas'],['mensagens','Mensagens']].map(([v,n])=>`<option value="${v}" ${f.tipo===v?'selected':''}>${n}</option>`).join('')}</select></div><div id="activityResults">${activityResults()}</div></section>`;
}
function activityResults() {
  const f=filters.atividade,items=state.historico.slice().reverse().filter(h=>(!f.tipo||eventType(h)===f.tipo)&&matches(f.q,h.texto));
  if(!items.length) return empty('Nenhum registro encontrado','As operações realizadas na central aparecem neste histórico.');
  let previous='';
  return `<div class="full-activity">${items.map(h=>{const d=day(h.data),heading=d!==previous?`<div class="activity-date-label">${esc(d).toUpperCase()}</div>`:'';previous=d;return heading+activityItem(h);}).join('')}</div>`;
}

function dialogHead(eyebrow,title,subtitle,dialogId='detailDialog') {
  return `<div class="dialog-head"><div><div class="eyebrow">${esc(eyebrow)}</div><h2 id="${dialogId==='detailDialog'?'detailTitle':'formTitle'}">${esc(title)}</h2>${subtitle?`<p>${esc(subtitle)}</p>`:''}</div><button class="icon-button" data-close="${dialogId}" aria-label="Fechar janela">${icon('close')}</button></div>`;
}
function fact(name,value,html=false) {
  return `<div class="detail-fact"><span>${esc(name)}</span><strong>${html?value:esc(value)}</strong></div>`;
}
function showDialog(id) {
  const dialog=$('#'+id);
  if (!dialog.open) dialog.showModal();
  setConnection(connected);
}
function refreshDetail() {
  if (!detail) return;
  if (detail.kind==='mission') openMission(detail.id,false);
  if (detail.kind==='robo') openRobot(detail.id,false);
  if (detail.kind==='area') openArea(detail.id,false);
}
function missionSteps(m) {
  const cancelled=m.status==='CANCELADA';
  const current=m.status==='CONCLUIDA'||cancelled?3:m.status==='EM_ANDAMENTO'?2:m.robo?1:0;
  const completedBeforeCancel=m.previsto>0?2:m.robo?1:0;
  return `<div class="detail-steps" aria-label="Etapas da missão">${['Registrada','Robô reservado','Em campo',cancelled?'Cancelada':'Concluída'].map((name,i)=>`<div class="detail-step ${cancelled?(i===3?'cancelled':i<=completedBeforeCancel?'done':''):i<current?'done':i===current?'current':''}">${name}</div>`).join('')}</div>`;
}
async function openMission(id,show=true) {
  const m=state.missoes.find(m=>m.id===id);
  if (!m) { toast('Missão não encontrada.',true);return; }
  detail={kind:'mission',id};
  const request=++detailRequest;
  const a=areaOf(m),r=roboOf(m);
  const finished=['CONCLUIDA','CANCELADA'].includes(m.status);
  const estimate=m.status==='PENDENTE'?(m.estimativa===null?'A confirmar':`${m.estimativa} pontos`):`${m.previsto} pontos`;
  $('#detailContent').innerHTML=dialogHead(`MISSÃO / ${m.id}`,m.descricao,label(m.tipo))+
    `<div class="dialog-body">${missionSteps(m)}<div class="detail-grid">${fact('Estado da missão',status(m.status),true)}${fact('Prioridade',priority(m.prioridade),true)}${fact('Área de operação',a?.nome||m.area)}${fact('Terreno e risco',`${label(a.terreno)} · ${label(a.risco)}`)}${fact('Responsável',r?`${r.id} · ${r.nome}`:'Aguardando atribuição')}${fact(m.status==='PENDENTE'?'Estimativa de consumo':'Previsão fixada no início',estimate)}${m.tipo==='TRANSPORTE_SUPRIMENTOS'?fact('Suprimentos',`${m.peso} kg`):''}${finished?fact('Consumo aplicado',`${m.aplicado} pontos`):''}</div>
    ${finished?`<section class="dialog-section"><h3>Resultado da missão</h3><p class="result-text">${esc(m.resultado)}</p></section>`:''}
    ${m.status==='PENDENTE'&&!m.robo?`<section class="dialog-section"><h3>Escolher unidade</h3><p class="small muted">Sugestão por menor consumo, maior bateria e identificador.</p><div id="candidateList"><div class="loading-state" style="min-height:110px"><span class="spinner"></span></div></div></section>`:''}
    ${m.status==='PENDENTE'&&m.robo?`<div class="info-box">${esc(r.nome)} está reservado para esta missão. A energia será revalidada ao iniciar. A missão permanece na fila até esse momento.</div>`:''}
    ${m.status==='EM_ANDAMENTO'?`<div class="info-box positive">Operação iniciada com ${m.previsto} pontos previstos. Concluir a execução aplica o consumo e libera o robô.</div>`:''}
    ${r?`<div style="margin-top:18px"><button class="text-button" data-open-robo="${esc(r.id)}">Abrir unidade ${esc(r.id)} ${icon('arrow')}</button></div>`:''}</div>
    <div class="dialog-footer">${!finished?button('Cancelar missão','close',`data-cancel-mission="${esc(id)}"`,'danger'):''}${m.status==='PENDENTE'&&m.robo?button('Retirar reserva',null,`data-command="removerAtribuicao" data-missao="${esc(id)}"`):''}${m.status==='PENDENTE'&&m.robo?button('Iniciar missão','play',`data-command="iniciar" data-missao="${esc(id)}"`,'primary'):''}${m.status==='EM_ANDAMENTO'?button('Executar e concluir','check',`data-command="executar" data-missao="${esc(id)}"`,'primary'):''}${finished?button('Fechar',null,'data-close="detailDialog"'):''}</div>`;
  if(show) showDialog('detailDialog'); else setConnection(connected);
  if(m.status==='PENDENTE'&&!m.robo) {
    try {
      const data=await api(`/api/candidatos?id=${encodeURIComponent(id)}`);
      if(request!==detailRequest||detail?.kind!=='mission'||detail.id!==id||!$('#candidateList')) return;
      $('#candidateList').innerHTML=data.candidatos.map(c=>`<article class="candidate ${c.apto?'':'dim'}"><div class="candidate-icon">${icon(robotIcon(c.tipo))}</div><div class="candidate-info"><strong>${esc(c.robo)} · ${esc(c.nome)}${data.sugestao===c.robo?'<span class="recommended">SUGESTÃO</span>':''}</strong><span>${label(c.tipo)} · bateria ${c.bateria}%</span></div>${c.apto?`<div class="candidate-values"><strong>${c.consumo} pontos</strong><span>Restam ${c.restante}%</span></div>${button('Reservar',null,`data-command="atribuir" data-robo="${esc(c.robo)}" data-missao="${esc(id)}"`,'compact')}`:`<span class="candidate-reason">${esc(c.motivo)}</span>`}</article>`).join('') || empty('Frota vazia','Adicione um robô para esta operação.','',true);
      if(!data.sugestao) $('#candidateList').insertAdjacentHTML('afterbegin','<div class="info-box warning">Nenhuma unidade disponível atende a esta missão. Confira tipo, terreno, capacidade e bateria.</div>');
      setConnection(connected);
    } catch(error) {
      if(request===detailRequest&&$('#candidateList')) $('#candidateList').innerHTML=`<p class="inline-error" role="alert">${esc(error.message)}</p>`;
    }
  }
}
const expandedMessages=new Set();
function openRobot(id,show=true) {
  const r=state.robos.find(r=>r.id===id);
  if(!r) return;
  detail={kind:'robo',id};detailRequest++;
  const messages=expandedMessages.has(id)?r.mensagens.slice().reverse():r.mensagens.slice(-5).reverse();
  const blocked=Boolean(r.missao);
  $('#detailContent').innerHTML=dialogHead(`UNIDADE / ${r.id}`,r.nome,`${label(r.tipo)} · ${plural(r.realizadas,'missão concluída','missões concluídas')}`)+
    `<div class="dialog-body"><div class="robot-stage" style="margin-top:0">${robotArt(r.tipo)}<span class="stage-id">UNIT / ${esc(id)}</span></div><div class="detail-grid">${fact('Estado atual',status(r.status),true)}${fact(r.tipo==='DRONE'?'Altitude máxima':'Capacidade de carga',`${r.capacidade} ${r.tipo==='DRONE'?'m':'kg'}`)}${fact('Bateria atual',`${r.bateria}%`)}${fact('Missão em curso ou reservada',r.missao||'Nenhuma')}</div>${battery(r.bateria)}
    <div class="dialog-section" style="margin-top:22px"><h3>Operações da unidade</h3><div class="communication-actions">${button('Recarregar','bolt',`data-command="recarregar" data-robo="${esc(id)}" data-blocked="${blocked}"`)}${r.status==='EM_MANUTENCAO'?button('Finalizar manutenção','check',`data-command="finalizarManutencao" data-robo="${esc(id)}"`):button('Iniciar manutenção','tool',`data-command="iniciarManutencao" data-robo="${esc(id)}" data-blocked="${blocked}"`)}</div>${r.missao?`<div class="info-box">Para recarregar ou entrar em manutenção, finalize a operação ou retire a reserva. <button class="text-button" data-open-mission="${esc(r.missao)}">Abrir ${esc(r.missao)} ${icon('arrow')}</button></div>`:''}</div>
    <section class="dialog-section"><h3>Comunicação com a unidade</h3><div class="message-list">${messages.map(h=>`<div class="message-item"><time datetime="${esc(h.data)}">${when(h.data)}</time>${esc(h.texto)}</div>`).join('') || '<p class="small muted">Nenhuma mensagem registrada.</p>'}</div>${r.mensagens.length>5?`<button class="text-button" style="margin-top:12px" data-expand-messages="${esc(id)}">${expandedMessages.has(id)?'Mostrar últimas mensagens':`Ver todas as ${r.mensagens.length} mensagens`}</button>`:''}
    <form id="communicationForm" class="communication-form" data-robo="${esc(id)}"><div class="form-error" role="alert" hidden></div><div class="field"><label for="robotMessage">Mensagem</label><textarea id="robotMessage" name="mensagem" required maxlength="2000" placeholder="Escreva uma instrução ou registre uma resposta…"></textarea></div><div class="communication-actions"><button class="button compact" type="submit" name="acao" value="receberInformacao" data-submit>${icon('send')}Registrar resposta</button><button class="button primary compact" type="submit" name="acao" value="enviarInstrucao" data-submit>${icon('send')}Enviar instrução</button></div></form></section></div><div class="dialog-footer">${button('Fechar',null,'data-close="detailDialog"')}</div>`;
  if(show) showDialog('detailDialog'); else setConnection(connected);
}
function openArea(id,show=true) {
  const a=state.areas.find(a=>a.id===id);
  if(!a)return;
  detail={kind:'area',id};detailRequest++;
  const missions=state.missoes.filter(m=>m.area===id);
  $('#detailContent').innerHTML=dialogHead(`ÁREA / ${a.id}`,a.nome,`${a.localizacao} · terreno ${label(a.terreno).toLowerCase()}`)+
    `<div class="dialog-body"><div class="area-landscape ${a.terreno}" style="border-radius:10px;margin-bottom:22px">${terrainArt(a.terreno)}</div><p class="result-text">${esc(a.descricao)}</p><div class="detail-grid" style="margin-top:22px">${fact('Terreno',label(a.terreno))}${fact('Nível de risco',status(a.risco),true)}${fact('Missões ativas',activeIn(id).length)}${fact('Total de missões',missions.length)}</div><section class="dialog-section"><h3>Atualizar nível de risco</h3><form id="riskForm" data-area="${esc(id)}"><div class="form-error" role="alert" hidden></div><div class="field"><label for="areaRisk">Risco atual da área</label><select id="areaRisk" name="risco">${['BAIXO','MEDIO','ALTO'].map(v=>`<option value="${v}" ${v===a.risco?'selected':''}>${label(v)}</option>`).join('')}</select><small>O risco influencia o consumo das missões que ainda vão iniciar.</small></div><button class="button compact" type="submit" data-submit>${icon('check')}Atualizar risco</button></form></section><section class="dialog-section"><h3>Missões nesta área</h3>${missions.map(m=>`<div class="candidate"><span class="candidate-icon">${icon(missionIcon(m.tipo))}</span><div class="candidate-info"><button class="mission-title" data-open-mission="${esc(m.id)}">${esc(m.id)} · ${esc(m.descricao)}</button></div>${missionStatus(m)}</div>`).join('') || '<p class="small muted">Esta área ainda não recebeu missões.</p>'}</section></div><div class="dialog-footer">${button('Fechar',null,'data-close="detailDialog"')}${button('Criar missão nesta área','plus',`data-new="missao" data-area="${esc(id)}"`,'primary')}</div>`;
  if(show)showDialog('detailDialog');else setConnection(connected);
}

function field(name,title,input,hint='') {
  return `<div class="field" id="field-${name}"><label for="input-${name}">${title}</label>${input}${hint?`<small>${hint}</small>`:''}</div>`;
}
function input(name,placeholder='',attrs='') {
  return `<input id="input-${name}" name="${name}" placeholder="${placeholder}" ${attrs}>`;
}
function select(name,values,selected) {
  return `<select id="input-${name}" name="${name}" required>${values.map(v=>`<option value="${Array.isArray(v)?esc(v[0]):v}" ${selected===(Array.isArray(v)?v[0]:v)?'selected':''}>${Array.isArray(v)?esc(v[1]):label(v)}</option>`).join('')}</select>`;
}
function nextId(items,prefix) {
  let n=1;while(items.some(x=>x.id===prefix+n))n++;return prefix+n;
}
function showForm(kind,preset={}) {
  if(kind==='missao'&&!state.areas.length) {
    showHelpMessage('Cadastre uma área primeiro','Uma missão precisa de uma área de operação cadastrada.',button('Cadastrar área','plus','data-new="area"','primary'));
    return;
  }
  let title,subtitle,fields,action,submit;
  if(kind==='robo') {
    title='Adicionar robô';subtitle='Uma nova unidade para a sua equipe.';action='cadastrarRobo';submit='Adicionar à frota';
    fields=field('tipo','Tipo de unidade',select('tipo',['DRONE','TERRESTRE','ANFIBIO'],'DRONE'))+
      `<div class="field-row">${field('id','Identificador',input('id','D3',`value="${nextId(state.robos,'D')}" required maxlength="80" autocomplete="off"`),'Único na frota; diferencia maiúsculas e minúsculas.')}${field('nome','Nome da unidade',input('nome','Ex.: Explorador', 'required maxlength="120" autocomplete="off"'))}</div>`+
      field('capacidade','Altitude máxima (m)',input('capacidade','120','type="number" min="1" max="2147483647" step="1" value="120" required'))+
      field('bateria','Bateria inicial <span class="field-value" id="batteryValue">100%</span>',input('bateria','','type="range" min="0" max="100" step="1" value="100"'),'A unidade com bateria zero inicia indisponível.');
  } else if(kind==='area') {
    title='Nova área de operação';subtitle='Defina o terreno e os riscos do próximo resgate.';action='cadastrarArea';submit='Cadastrar área';
    fields=`<div class="field-row">${field('id','Identificador',input('id','AR4',`value="${nextId(state.areas,'AR')}" required maxlength="80" autocomplete="off"`))}${field('nome','Nome da área',input('nome','Ex.: Distrito norte','required maxlength="120"'))}</div>`+
      field('localizacao','Localização',input('localizacao','Ex.: Setor 4','required maxlength="180"'))+
      `<div class="field-row">${field('terreno','Tipo de terreno',select('terreno',['URBANO','IRREGULAR','ALAGADO'],'URBANO'))}${field('risco','Nível de risco',select('risco',['BAIXO','MEDIO','ALTO'],'BAIXO'))}</div>`+
      field('descricao','Descrição','<textarea id="input-descricao" name="descricao" placeholder="Condições e características da área…" required maxlength="2000"></textarea>');
  } else {
    title='Nova missão';subtitle='Registre o chamado. A central organiza os próximos passos.';action='registrarMissao';submit='Registrar missão';
    fields=`<div class="field-row">${field('id','Identificador',input('id','M5',`value="${nextId(state.missoes,'M')}" required maxlength="80" autocomplete="off"`))}${field('prioridade','Prioridade',select('prioridade',['BAIXA','MEDIA','ALTA'],'MEDIA'))}</div>`+
      field('descricao','Objetivo da missão','<textarea id="input-descricao" name="descricao" placeholder="Ex.: Levar suprimentos ao setor norte" required maxlength="2000"></textarea>')+
      field('tipo','Tipo de missão',select('tipo',['RECONHECIMENTO','LOCALIZACAO_PESSOAS','TRANSPORTE_SUPRIMENTOS'],'RECONHECIMENTO'))+
      field('area','Área de operação',select('area',state.areas.map(a=>[a.id,`${a.id} · ${a.nome} (${label(a.terreno)})`]),preset.area||state.areas[0].id))+
      field('peso','Peso dos suprimentos (kg)',input('peso','20','type="number" min="1" max="2147483647" step="1" value="20"'),'O peso deve caber na capacidade do robô escolhido.')+
      '<div class="info-box">A missão entra no fim da fila. A prioridade fica disponível para a decisão manual da equipe.</div>';
  }
  $('#formContent').innerHTML=dialogHead('NOVO CADASTRO',title,subtitle,'formDialog')+
    `<div class="dialog-body"><form id="entityForm" data-action="${action}" data-kind="${kind}"><div class="form-error" role="alert" hidden></div>${fields}<div class="form-buttons">${button('Voltar',null,'data-close="formDialog"')}<button type="submit" class="button primary" data-submit>${icon('plus')}${submit}</button></div></form></div>`;
  if(kind==='missao')updateCargoField();
  if(kind==='robo')$('#input-id').dataset.autoValue=$('#input-id').value;
  showDialog('formDialog');
}
function updateCargoField() {
  if($('#entityForm')?.dataset.kind!=='missao')return;
  const transport=$('#input-tipo').value==='TRANSPORTE_SUPRIMENTOS';
  $('#field-peso').hidden=!transport;
  $('#input-peso').disabled=!transport;
  $('#input-peso').required=transport;
}
function confirmCancellation(id) {
  const m=state.missoes.find(m=>m.id===id);
  const started=m.status==='EM_ANDAMENTO';
  $('#formContent').innerHTML=dialogHead('CONFIRMAR CANCELAMENTO',`Cancelar missão ${id}?`,'Esta operação encerra a missão.','formDialog')+
    `<div class="dialog-body"><p class="result-text">${started?`A missão já está em andamento. O cancelamento aplica os ${m.previsto} pontos previstos de bateria e libera o robô, sem contar uma missão concluída.`:'A missão ainda não iniciou. O cancelamento libera o robô reservado e não consome bateria.'}</p><form id="confirmForm" data-action="cancelar" data-missao="${esc(id)}"><div class="form-error" role="alert" hidden style="margin-top:15px"></div><div class="form-buttons">${button('Manter missão',null,'data-close="formDialog"')}<button type="submit" class="button danger" data-submit>${icon('close')}Confirmar cancelamento</button></div></form></div>`;
  showDialog('formDialog');
}
function confirmReset() {
  $('#formContent').innerHTML=dialogHead('REINICIAR DEMONSTRAÇÃO','Começar de novo?','A central visual volta aos dados iniciais.','formDialog')+
    `<div class="dialog-body"><p class="result-text">Os cadastros, estados e históricos desta versão visual serão substituídos por quatro robôs, três áreas e quatro missões de exemplo.</p><p class="result-text" style="margin-top:10px">O arquivo da aplicação de console é independente.</p><form id="resetForm"><div class="form-error" role="alert" hidden></div><div class="field" style="margin-top:20px"><label for="resetConfirmation">Digite REINICIAR para confirmar</label><input id="resetConfirmation" name="confirmacao" autocomplete="off" required pattern="REINICIAR"></div><div class="form-buttons">${button('Voltar',null,'data-close="formDialog"')}<button class="button danger" type="submit" data-submit data-blocked="true">${icon('refresh')}Reiniciar demonstração</button></div></form></div>`;
  showDialog('formDialog');
}
function showHelpMessage(title,text,action) {
  $('#formContent').innerHTML=dialogHead('CENTRAL DE OPERAÇÕES',title,'','formDialog')+
    `<div class="dialog-body"><p class="result-text">${esc(text)}</p></div><div class="dialog-footer">${button('Fechar',null,'data-close="formDialog"')}${action}</div>`;
  showDialog('formDialog');
}
function showHelp() {
  const steps=[
    ['Prepare a operação','Cadastre áreas e robôs. A demonstração já começa com uma equipe e quatro chamados de exemplo.'],
    ['Escolha uma missão','Abra uma missão pendente para comparar as unidades compatíveis, o consumo e a bateria restante.'],
    ['Reserve e inicie','Reserve um robô e depois inicie a missão. A central revalida a energia e mantém os vínculos entre os objetos.'],
    ['Conclua e acompanhe','Execute a missão para registrar o resultado, descontar a bateria e liberar a unidade. Consulte os relatórios e o histórico.'],
  ];
  $('#formContent').innerHTML=dialogHead('GUIA RÁPIDO','Sua primeira operação','Uma central de resgate nas suas mãos.','formDialog')+
    `<div class="dialog-body"><div class="help-steps">${steps.map(([title,text])=>`<div class="help-step"><div><strong>${title}</strong><p>${text}</p></div></div>`).join('')}</div><div class="info-box">As alterações são salvas automaticamente. Ao reabrir o servidor, a central continua de onde parou. Atalho de busca: <strong>Ctrl K</strong>.</div></div><div class="dialog-footer">${button('Reiniciar demonstração','refresh','data-reset-demo','danger')}${button('Entendi','check','data-close="formDialog"','primary')}</div>`;
  showDialog('formDialog');
}
function openSearch() {
  if(!state)return;
  $('#searchContent').innerHTML=`<div class="search-dialog-head">${icon('search')}<input type="search" id="globalQuery" placeholder="Busque missões, robôs ou áreas…" aria-label="Busca geral" autocomplete="off"><button class="icon-button" data-close="searchDialog" aria-label="Fechar busca">${icon('close')}</button></div><div class="search-results" id="globalResults"></div>`;
  updateSearch('');
  showDialog('searchDialog');
  $('#globalQuery').focus();
}
function updateSearch(q) {
  const groups=[
    ['MISSÕES',state.missoes.filter(m=>matches(q,m.id,m.descricao,label(m.tipo))).slice(0,6).map(m=>`<button class="search-result" data-search-mission="${esc(m.id)}">${icon(missionIcon(m.tipo))}<span><strong>${esc(m.descricao)}</strong><small>${esc(m.id)} · ${label(m.tipo)}</small></span>${icon('arrow')}</button>`)],
    ['ROBÔS',state.robos.filter(r=>matches(q,r.id,r.nome,label(r.tipo))).slice(0,6).map(r=>`<button class="search-result" data-search-robo="${esc(r.id)}">${icon(robotIcon(r.tipo))}<span><strong>${esc(r.nome)}</strong><small>${esc(r.id)} · ${label(r.tipo)}</small></span>${icon('arrow')}</button>`)],
    ['ÁREAS',state.areas.filter(a=>matches(q,a.id,a.nome,a.localizacao)).slice(0,6).map(a=>`<button class="search-result" data-search-area="${esc(a.id)}">${icon('map')}<span><strong>${esc(a.nome)}</strong><small>${esc(a.id)} · ${esc(a.localizacao)}</small></span>${icon('arrow')}</button>`)],
  ];
  const html=groups.filter(([,items])=>items.length).map(([name,items])=>`<div class="search-category">${name}</div>${items.join('')}`).join('');
  $('#globalResults').innerHTML=html||empty('Nenhum resultado','Tente outro identificador, nome ou descrição.','',true);
}

function updateFilteredResults(group) {
  const targets={missoes:['#missionResults',missionResults],frota:['#fleetResults',fleetResults],areas:['#areaResults',areaResults],atividade:['#activityResults',activityResults]};
  const target=targets[group];
  if(target&&$(target[0]))$(target[0]).innerHTML=target[1]();
  setConnection(connected);
}
function closeNavigation() {
  $('#sidebar').classList.remove('open');$('#navBackdrop').hidden=true;
  $('#menuButton').setAttribute('aria-expanded','false');
}
function setTheme(theme) {
  document.documentElement.dataset.theme=theme;
  $('#themeButton').innerHTML=icon(theme==='dark'?'sun':'moon');
  $('#themeButton').setAttribute('aria-label',theme==='dark'?'Ativar tema claro':'Ativar tema escuro');
  try {localStorage.setItem('resgate-theme',theme);}catch{}
}
document.addEventListener('click',event=>{
  const el=event.target.closest('button,a');
  if(!el||el.disabled)return;
  if(el.hasAttribute('data-close')) { $('#'+el.dataset.close).close();return; }
  if(el.hasAttribute('data-new')) {showForm(el.dataset.new,{area:el.dataset.area});return;}
  if(el.hasAttribute('data-open-mission')) {openMission(el.dataset.openMission);return;}
  if(el.hasAttribute('data-open-robo')) {openRobot(el.dataset.openRobo);return;}
  if(el.hasAttribute('data-open-area')) {openArea(el.dataset.openArea);return;}
  if(el.hasAttribute('data-cancel-mission')) {confirmCancellation(el.dataset.cancelMission);return;}
  if(el.hasAttribute('data-reset-demo')) {confirmReset();return;}
  if(el.hasAttribute('data-command')) {
    const params={acao:el.dataset.command};
    ['robo','missao','area','risco'].forEach(k=>{if(el.dataset[k])params[k]=el.dataset[k];});
    mutate(params);return;
  }
  if(el.hasAttribute('data-search-mission')){$('#searchDialog').close();openMission(el.dataset.searchMission);return;}
  if(el.hasAttribute('data-search-robo')){$('#searchDialog').close();openRobot(el.dataset.searchRobo);return;}
  if(el.hasAttribute('data-search-area')){$('#searchDialog').close();openArea(el.dataset.searchArea);return;}
  if(el.hasAttribute('data-expand-messages')) {
    const id=el.dataset.expandMessages;
    if(expandedMessages.has(id))expandedMessages.delete(id);else expandedMessages.add(id);
    openRobot(id,false);return;
  }
  if(el.hasAttribute('data-refresh-report')){loadReport();return;}
  if(el.hasAttribute('data-print')){window.print();return;}
  if(el.hasAttribute('data-retry')){loadState(true);return;}
});
document.addEventListener('input',event=>{
  const el=event.target;
  if(el.matches('input[data-filter]')){
    filters[el.dataset.filterGroup][el.dataset.filter]=el.value;
    updateFilteredResults(el.dataset.filterGroup);
  }
  if(el.id==='globalQuery')updateSearch(el.value);
  if(el.id==='input-bateria')$('#batteryValue').textContent=el.value+'%';
  if(el.id==='resetConfirmation'){
    const b=$('#resetForm [data-submit]');
    b.dataset.blocked=String(el.value!=='REINICIAR');
    b.disabled=!connected||busy||el.value!=='REINICIAR';
  }
});
document.addEventListener('change',event=>{
  const el=event.target;
  if(el.matches('select[data-filter]')){
    filters[el.dataset.filterGroup][el.dataset.filter]=el.value;
    updateFilteredResults(el.dataset.filterGroup);
  }
  if(el.id==='input-tipo'&&$('#entityForm')?.dataset.kind==='missao')updateCargoField();
  if(el.id==='input-tipo'&&$('#entityForm')?.dataset.kind==='robo'){
    const drone=el.value==='DRONE',prefix=drone?'D':el.value==='ANFIBIO'?'A':'T';
    const id=$('#input-id');
    if(id.value===id.dataset.autoValue||!id.value){id.value=nextId(state.robos,prefix);id.dataset.autoValue=id.value;}
    $('#field-capacidade label').textContent=drone?'Altitude máxima (m)':'Capacidade de carga (kg)';
    $('#input-capacidade').value=drone?120:el.value==='ANFIBIO'?30:50;
  }
});
document.addEventListener('submit',async event=>{
  const form=event.target;
  const formId=form.getAttribute('id');
  if(!['entityForm','communicationForm','riskForm','confirmForm','resetForm'].includes(formId))return;
  event.preventDefault();
  const params=Object.fromEntries(new FormData(form).entries());
  const options={errorEl:$('.form-error',form)};
  if(formId==='entityForm'){
    params.acao=form.dataset.action;
    if(form.dataset.kind==='missao'&&params.tipo!=='TRANSPORTE_SUPRIMENTOS')params.peso='0';
    options.closeForm=true;
  }else if(formId==='communicationForm'){
    params.acao=event.submitter?.value||'enviarInstrucao';params.robo=form.dataset.robo;
  }else if(formId==='riskForm'){
    params.acao='atualizarRisco';params.area=form.dataset.area;
  }else if(formId==='confirmForm'){
    params.acao=form.dataset.action;params.missao=form.dataset.missao;options.closeForm=true;
  }else{
    params.acao='reiniciarDemo';options.closeForm=true;options.closeDetail=true;
  }
  await mutate(params,options);
});
$('#menuButton').addEventListener('click',()=>{
  const opened=$('#sidebar').classList.toggle('open');
  $('#navBackdrop').hidden=!opened;$('#menuButton').setAttribute('aria-expanded',String(opened));
});
$('#navBackdrop').addEventListener('click',closeNavigation);
$('#helpButton').addEventListener('click',showHelp);
$('#searchButton').addEventListener('click',openSearch);
$('#themeButton').addEventListener('click',()=>setTheme(document.documentElement.dataset.theme==='dark'?'light':'dark'));
$('#retryButton').addEventListener('click',()=>loadState(true));
window.addEventListener('hashchange',()=>{
  if(location.hash==='#main')return;
  page=pages[location.hash.slice(1)]?location.hash.slice(1):'inicio';
  closeNavigation();render();window.scrollTo({top:0,behavior:'instant'});
});
document.addEventListener('keydown',event=>{
  if((event.ctrlKey||event.metaKey)&&event.key.toLowerCase()==='k'){
    event.preventDefault();if($('#searchDialog').open)$('#searchDialog').close();else openSearch();
  }
  if(event.key==='Escape')closeNavigation();
});
$('#detailDialog').addEventListener('close',()=>{detail=null;detailRequest++;});
document.querySelectorAll('dialog').forEach(dialog=>dialog.addEventListener('click',event=>{
  if(event.target!==dialog)return;
  const r=dialog.getBoundingClientRect();
  if(event.clientX<r.left||event.clientX>r.right||event.clientY<r.top||event.clientY>r.bottom)dialog.close();
}));
document.addEventListener('visibilitychange',()=>{if(!document.hidden&&!busy)loadState();});
hydrate();
try{setTheme(localStorage.getItem('resgate-theme')==='dark'?'dark':'light');}catch{setTheme('light');}
render();
loadState(true);
setInterval(()=>{if(!document.hidden&&!busy)loadState();},8000);
