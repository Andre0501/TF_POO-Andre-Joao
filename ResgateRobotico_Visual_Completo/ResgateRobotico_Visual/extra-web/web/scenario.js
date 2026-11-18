'use strict';

// A reprodução é uma camada visual local. O cenário usa apenas os nomes e
// riscos cadastrados para montar os marcadores e abrir os detalhes existentes.
(() => {
  const catalog = {
    flood: {
      label: 'Enchente', icon: 'waves', terrain: 'Vale fluvial',
      legend: 'Mancha de inundação',
      stages: [
        ['Subida', 'Subida do rio', 'A chuva na bacia aumenta a vazão e o nível da água sobe junto às margens.', 'Água ainda no leito'],
        ['Extravaso', 'Transbordamento', 'A água ultrapassa a margem e se espalha pelas ruas e terrenos mais baixos.', 'Vias baixas atingidas'],
        ['Bloqueios', 'Acessos interrompidos', 'A inundação avança pela planície e interrompe alguns acessos do bairro ribeirinho.', 'Trechos baixos isolados'],
      ],
    },
    slope: {
      label: 'Deslizamento', icon: 'mountain', terrain: 'Encosta urbana',
      legend: 'Faixa de deslocamento',
      stages: [
        ['Saturação', 'Solo encharcado', 'A chuva persistente se infiltra no solo e reduz a estabilidade de um trecho da encosta.', 'Solo saturado'],
        ['Ruptura', 'Ruptura da encosta', 'Uma massa de terra e pedras se desprende e desce pelo trecho de maior declive.', 'Material em deslocamento'],
        ['Deposição', 'Deposição de detritos', 'A terra se acumula no pé da encosta e cobre parte da via que passa pelo bairro.', 'Acesso coberto por detritos'],
      ],
    },
    storm: {
      label: 'Tempestade', icon: 'rain', terrain: 'Periferia urbana',
      legend: 'Faixa de precipitação',
      stages: [
        ['Chegada', 'Chuva se intensifica', 'Uma faixa de chuva chega ao bairro e reduz a visibilidade nas primeiras quadras atingidas.', 'Visibilidade em queda'],
        ['Rajadas', 'Chuva forte e rajadas', 'O vento inclina a chuva e as quadras sob precipitação mais intensa perdem contraste.', 'Visibilidade reduzida'],
        ['Acúmulo', 'Impacto nas vias', 'A água se acumula em depressões. Poças e galhos caídos limitam a passagem em alguns trechos.', 'Passagem limitada em pontos baixos'],
      ],
    },
  };
  const phasePoints = [0, 0.5, 0.83];
  const seconds = 22;
  const reducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)');
  const riskColors = {BAIXO:'green',MEDIO:'amber',ALTO:'red'};
  const limited = (value,min,max) => Math.max(min,Math.min(max,value));
  let key = 'flood';
  let progress = 0.48;
  let playing = false;
  let showImpact = true;
  let frame = 0;
  let previous = 0;
  let latestPaint = 0;
  let view = null;
  let drawIcon;

  // Os caminhos seguem as feições das imagens fictícias no mesmo sistema de
  // coordenadas usado para o enquadramento da foto, sem medidas geográficas.
  const floodRiver = 'M346 0C358 65 431 156 469 233C514 318 569 395 558 476C549 561 508 649 446 715L358 800H816C904 710 924 610 899 508C890 374 817 274 728 200C648 137 612 70 589 0Z';
  const floodMargin = 'M577 0C603 85 644 152 717 209C794 270 853 368 871 457C914 624 869 720 788 800H862C939 716 949 606 936 502C919 365 865 271 773 198C692 132 657 69 628 0Z';
  const floodSpread = 'M615 0H655C691 67 730 121 810 186C914 257 968 344 1003 447L1058 467L1070 510L1011 552C1018 625 993 704 961 755L941 800H852C930 723 949 613 929 511C907 369 850 273 762 201C682 139 641 62 615 0Z';
  const slopeScar = 'M243 154C275 135 320 157 339 193L411 259L465 324L544 379L622 412L740 525L801 565L914 582L955 611L955 648L881 666L814 648L760 625L677 598L619 543L558 494L476 467L406 417L354 365L306 290L270 242Z';
  const slopeTrack = 'M288 179C328 254 411 343 486 392S614 449 681 524S797 607 893 625';

  function overlay() {
    return `<svg class="map-effects" viewBox="0 0 1200 800" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
      <defs>
        <pattern id="waterPattern" width="94" height="50" patternUnits="userSpaceOnUse">
          <path d="M0 12q14-5 34 0m14 18q14 4 32 0M31 43q16-3 36 0" fill="none" stroke="#b7f0ff" stroke-width="1.2" opacity=".58"/>
        </pattern>
        <pattern id="debrisPattern" width="15" height="15" patternUnits="userSpaceOnUse">
          <path d="M-4 5 5-4m-2 16L16-1m-3 18 5-5" stroke="#ffce91" stroke-width="1.1" opacity=".66"/>
        </pattern>
        <pattern id="rainPattern" width="126" height="100" patternUnits="userSpaceOnUse">
          <path d="m7 7-5 18m30 6-6 21m26-42-6 19m31 16-7 25m22-65-5 19m34 7-6 21m-87 21-5 17m33-16-5 19m50-14-7 18" stroke="#d5efff" stroke-width="1.3" stroke-linecap="round"/>
        </pattern>
        <radialGradient id="rainMass" cx="52%" cy="44%" r="55%">
          <stop offset="0" stop-color="#cce8ef" stop-opacity=".54"/><stop offset=".52" stop-color="#accddd" stop-opacity=".32"/><stop offset="1" stop-color="#a2c0d3" stop-opacity="0"/>
        </radialGradient>
        <clipPath id="slipClip"><path d="${slopeScar}"/></clipPath>
      </defs>
      <g data-effect="flood">
        <path d="${floodRiver}" fill="#568aa1" opacity=".2"/>
        <path d="${floodRiver}" fill="url(#waterPattern)" opacity=".52"/>
        <g id="floodEdge">
          <path d="${floodMargin}" fill="#2588a7" fill-opacity=".32" stroke="#b6f2ff" stroke-opacity=".75" stroke-width="1.8"/>
          <path d="${floodMargin}" fill="url(#waterPattern)" opacity=".65"/>
        </g>
        <g id="floodReach">
          <path d="${floodSpread}" fill="#399cbf" fill-opacity=".42" stroke="#b6f2ff" stroke-width="2.1" stroke-dasharray="8 7"/>
          <path d="${floodSpread}" fill="url(#waterPattern)" opacity=".66"/>
        </g>
      </g>
      <g data-effect="slope">
        <path d="M234 146C245 130 278 127 311 140C345 160 371 198 388 231L371 250C335 213 310 180 284 178L251 183Z" fill="#ffbb6c" fill-opacity=".22" stroke="#ffd099" stroke-width="2" stroke-dasharray="6 6"/>
        <path id="slipArea" d="${slopeScar}" fill="#cf733a" fill-opacity=".48" stroke="#ffd09c" stroke-width="2" stroke-dasharray="7 7"/>
        <g clip-path="url(#slipClip)">
          <path id="slipFlow" d="${slopeTrack}" fill="none" stroke="#e79b5e" stroke-width="62" stroke-linecap="round" pathLength="100"/>
          <path id="slipGrain" d="${slopeTrack}" fill="none" stroke="#fff0c8" stroke-width="7" stroke-linecap="round" stroke-dasharray="1 8" pathLength="100"/>
        </g>
        <g id="slipFan">
          <path d="M768 560 835 567 914 582 967 613 968 649 921 679 869 682 815 658 772 628Z" fill="#c9733d" fill-opacity=".45" stroke="#ffd09c" stroke-width="2"/>
          <path d="M768 560 835 567 914 582 967 613 968 649 921 679 869 682 815 658 772 628Z" fill="url(#debrisPattern)" opacity=".6"/>
        </g>
      </g>
      <g data-effect="storm">
        <ellipse id="stormCloud" cx="810" cy="360" rx="630" ry="520" fill="url(#rainMass)"/>
        <rect id="stormRain" width="1200" height="800" fill="url(#rainPattern)"/>
        <path id="stormPuddles" d="M237 500q25-7 44 11l28 117q-15 16-44 5zm295-88q34-9 49 7l13 61q-15 29-50 12zm255 118q36-13 59 5l33 64q-18 28-83 14Z" fill="#80aabb" fill-opacity=".36" stroke="#daeaff" stroke-width="1.6" stroke-dasharray="5 5"/>
      </g>
    </svg>`;
  }

  function markerButtons(areas, {esc,label}) {
    const positions = [[78,32],[25,39],[81,73]];
    return areas.slice(0,positions.length).map((area,index) => {
      const [x,y] = positions[index];
      return `<button type="button" class="map-marker ${riskColors[area.risco] || 'gray'}" style="--x:${x}%;--y:${y}%" data-open-area="${esc(area.id)}" aria-label="${esc(area.nome)} · ${esc(area.id)} · risco ${esc(label(area.risco).toLowerCase())}"><span class="marker-pin" aria-hidden="true"><i></i></span><span class="marker-tag"><strong>${esc(area.id)}</strong><span>${esc(area.nome)}</span></span></button>`;
    }).join('');
  }

  function render(areas, ui) {
    const {icon} = ui;
    drawIcon = icon;
    const item = catalog[key];
    return `<section class="map-workbench" id="mapWorkbench" data-scenario="${key}">
      <div class="card map-panel">
        <header class="card-head map-head">
          <div class="map-heading"><span class="section-icon">${icon('map')}</span><div><h2>Mapa de operações</h2><p>Áreas cadastradas sobre cenário de treino</p></div></div>
          <button class="button compact layer-button" type="button" data-sim-action="impact" aria-pressed="${showImpact}" title="Alternar camada de impacto">${icon('layers')}<span>Impacto</span></button>
        </header>
        <div class="map-viewport">
          <img class="map-photo" src="/${key}.webp" alt="" fetchpriority="high" decoding="async">
          ${overlay()}
          <div class="map-grid" aria-hidden="true"></div>
          <span class="map-imagery"><i></i><span id="mapTerrain">${item.terrain}</span></span>
          ${markerButtons(areas,ui)}
          <div class="map-impact-caption" id="mapImpactCaption"><i></i><span>${item.legend}</span></div>
        </div>
        <div class="map-footer"><span>${icon('info')}Posições ilustrativas</span><span class="map-key"><i></i>Área cadastrada</span></div>
      </div>
      <aside class="card scenario-panel" aria-labelledby="scenarioTitle">
        <header class="card-head scenario-head"><h2 id="scenarioTitle">Simulação de desastre</h2><span class="training-tag">Fictício</span></header>
        <div class="scenario-content">
          <div class="scenario-picker" role="group" aria-label="Escolher desastre simulado">${Object.entries(catalog).map(([id,option])=>`<button type="button" class="scenario-option" data-sim-choice="${id}" aria-pressed="${id===key}">${icon(option.icon)}<span>${option.label}</span></button>`).join('')}</div>
          <div class="scenario-reading" aria-live="polite">
            <span class="scenario-step-label" id="scenarioStep"></span>
            <h3 id="phaseTitle"></h3>
            <p id="phaseDescription"></p>
          </div>
          <div class="scenario-impact"><span class="scenario-impact-icon">${icon('warning')}</span><span><span>Impacto simulado</span><strong id="phaseImpact"></strong></span></div>
          <div class="scenario-timeline">
            <div class="timeline-label"><label for="scenarioRange">Evolução do cenário</label><span id="scenarioPlaybackState">${playing?'Em reprodução':'Pausado'}</span></div>
            <input type="range" min="0" max="100" value="${Math.round(progress*100)}" step="1" id="scenarioRange">
            <div class="scenario-phases">${phasePoints.map((point,index)=>`<button type="button" data-sim-phase="${index}">${item.stages[index][0]}</button>`).join('')}</div>
          </div>
          <div class="playback-actions">${playButton(icon)}<button class="icon-button restart-button" type="button" data-sim-action="restart" title="Reiniciar cenário" aria-label="Reiniciar cenário">${icon('refresh')}</button></div>
        </div>
      </aside>
    </section>`;
  }

  function playButton(icon) {
    return `<button class="button playback-button" type="button" data-sim-action="play">${icon(playing?'pause':'play')}<span>${playing?'Pausar':'Reproduzir'}</span></button>`;
  }

  function mount() {
    const root = document.getElementById('mapWorkbench');
    if (!root) {
      view = null;
      pause();
      return;
    }
    const at = id => root.querySelector('#'+id);
    view = {
      root, image:root.querySelector('.map-photo'),
      terrain:at('mapTerrain'), impactCaption:at('mapImpactCaption'),
      options:[...root.querySelectorAll('[data-sim-choice]')],
      range:at('scenarioRange'), phases:[...root.querySelectorAll('[data-sim-phase]')],
      title:at('phaseTitle'), description:at('phaseDescription'), impact:at('phaseImpact'),
      step:at('scenarioStep'), state:at('scenarioPlaybackState'),
      play:root.querySelector('[data-sim-action="play"]'),
      layer:root.querySelector('[data-sim-action="impact"]'),
      effects: {
        floodEdge:at('floodEdge'), floodReach:at('floodReach'), water:at('waterPattern'),
        slipArea:at('slipArea'), slipFlow:at('slipFlow'), slipGrain:at('slipGrain'), slipFan:at('slipFan'),
        cloud:at('stormCloud'), rain:at('stormRain'), rainPattern:at('rainPattern'), puddles:at('stormPuddles'),
      },
    };
    syncScene();
  }

  function syncScene() {
    if (!view) return;
    const item = catalog[key];
    view.root.dataset.scenario = key;
    view.root.dataset.impact = String(showImpact);
    const photo = '/' + key + '.webp';
    if (view.image.getAttribute('src') !== photo) view.image.src = photo;
    view.terrain.textContent = item.terrain;
    view.impactCaption.querySelector('span').textContent = item.legend;
    view.layer.setAttribute('aria-pressed',String(showImpact));
    view.options.forEach(b=>b.setAttribute('aria-pressed',String(b.dataset.simChoice===key)));
    view.phases.forEach((b,i)=>{
      b.textContent = item.stages[i][0];
      b.setAttribute('aria-label',item.stages[i][1]);
    });
    view.phase = null;
    updatePlaybackButton();
    paint();
  }

  function updatePlaybackButton() {
    if (!view) return;
    view.play.innerHTML = drawIcon(playing?'pause':'play') + `<span>${playing?'Pausar':'Reproduzir'}</span>`;
    view.state.textContent = playing ? 'Em reprodução' : progress === 1 ? 'Concluído' : 'Pausado';
  }

  function paint() {
    if (!view) return;
    const index = Math.min(2,Math.floor(progress*3));
    const [,title,description,impact] = catalog[key].stages[index];
    const percent = Math.round(progress*100);
    view.range.value = String(percent);
    view.range.style.setProperty('--progress',percent+'%');
    if (view.phase !== index) {
      view.phase = index;
      view.step.textContent = `Fase ${index+1} de 3`;
      view.title.textContent = title;
      view.description.textContent = description;
      view.impact.textContent = impact;
      view.range.setAttribute('aria-valuetext',title);
      view.phases.forEach((b,i)=>{
        if (i===index) b.setAttribute('aria-current','step');
        else b.removeAttribute('aria-current');
      });
    }
    const p = reducedMotion?.matches ? phasePoints[index] : progress;
    const e = view.effects;
    if (key === 'flood') {
      e.floodEdge.style.opacity = String(limited((p-.08)*2.5,.18,1));
      e.floodReach.style.opacity = String(limited((p-.36)*1.9,0,1));
      e.water.setAttribute('patternTransform',`translate(${p*48} ${p*190})`);
    } else if (key === 'slope') {
      const travel = limited((p-.22)*1.85,0,1);
      e.slipArea.style.opacity = String(.25+travel*.65);
      e.slipFlow.setAttribute('stroke-dasharray',`${travel*100} 100`);
      e.slipFlow.style.opacity = String(travel*.52);
      e.slipGrain.style.opacity = String(Math.sin(travel*Math.PI)*.7);
      e.slipGrain.setAttribute('stroke-dashoffset',String(-p*130));
      e.slipFan.style.opacity = String(limited((p-.52)*3.6,0,1));
    } else {
      e.cloud.setAttribute('cx',String(840-p*525));
      e.cloud.style.opacity = String(.38+p*.58);
      e.rain.style.opacity = String(.14+p*.43);
      e.rainPattern.setAttribute('patternTransform',`translate(${-p*215} ${p*800})`);
      e.puddles.style.opacity = String(limited((p-.52)*2.6,0,1));
    }
  }

  function tick(now) {
    if (!playing || !view) return;
    if (previous) progress = Math.min(1,progress+Math.min(now-previous,70)/(seconds*1000));
    previous = now;
    if (now-latestPaint >= 32 || progress === 1) {
      paint();
      latestPaint = now;
    }
    if (progress === 1) pause();
    else frame = window.requestAnimationFrame(tick);
  }

  function pause() {
    playing = false;
    if (frame) window.cancelAnimationFrame(frame);
    frame = 0;
    previous = 0;
    updatePlaybackButton();
  }

  function togglePlayback() {
    if (playing) {
      pause();
      return;
    }
    if (progress === 1) progress = 0;
    playing = true;
    updatePlaybackButton();
    paint();
    frame = window.requestAnimationFrame(tick);
  }

  function seek(position) {
    pause();
    progress = position;
    paint();
    updatePlaybackButton();
  }

  document.addEventListener('click',event=>{
    const control = event.target.closest('#mapWorkbench button');
    if (!control || !view) return;
    const {simChoice,simPhase,simAction} = control.dataset;
    if (simChoice && simChoice !== key && catalog[simChoice]) {
      pause();
      key = simChoice;
      progress = 0;
      syncScene();
    }
    if (simPhase !== undefined) seek(phasePoints[Number(simPhase)]);
    if (simAction==='play') togglePlayback();
    if (simAction==='restart') seek(0);
    if (simAction==='impact') {
      showImpact = !showImpact;
      view.root.dataset.impact = String(showImpact);
      view.layer.setAttribute('aria-pressed',String(showImpact));
    }
  });

  document.addEventListener('input',event=>{
    if (event.target === view?.range) seek(Number(event.target.value)/100);
  });
  document.addEventListener('visibilitychange',()=>{ if (document.hidden && playing) pause(); });
  reducedMotion?.addEventListener('change',paint);
  window.ResgateScenario = Object.freeze({render,mount});
})();
