function iniciarProcesso(){
  const b=document.getElementById('botao-enviar')
  const f=document.getElementById('form-envio')
  const dados=new FormData(f)
  b.disabled=true
  fetch('/api/processar',{method:'POST',body:dados}).then(r=>r.json()).then(j=>{
    const id=j.execId||j.id||j.exec||j.uuid
    if(!id){b.disabled=false;alert('Falha ao iniciar processamento');return}
    location.href='loading.html?execId='+encodeURIComponent(id)
  }).catch(()=>{b.disabled=false;alert('Não foi possível iniciar o processamento')})
}
function iniciarAcompanhamento(id){
  if(!id){location.href='index.html';return}
  const etapa=document.getElementById('etapa')
  const detalhes=document.getElementById('detalhes')
  const barra=document.getElementById('progresso')
  const pct=document.getElementById('porcentagem')
  const log=document.getElementById('log')
  function tick(){
    fetch('/api/status?execId='+encodeURIComponent(id)).then(r=>r.json()).then(j=>{
      const p=j.porcentagem!=null?j.porcentagem:0
      etapa.textContent=j.etapa||'Processando'
      detalhes.textContent=j.detalhes||'Executando pipeline'
      barra.style.width=(p>100?100:p)+'%'
      pct.textContent=Math.round(p)+'%'
      if(j.log){log.textContent=j.log}
      if(j.concluido){location.href='results.html?execId='+encodeURIComponent(id);return}
      setTimeout(tick,1500)
    }).catch(()=>setTimeout(tick,2000))
  }
  tick()
}
function carregarResultados(id){
  if(!id){location.href='index.html';return}
  const ul=document.getElementById('lista-testes')
  const expl=document.getElementById('explicacoes')
  const dash=document.getElementById('dashboards')
  fetch('/api/testes?execId='+encodeURIComponent(id)).then(r=>r.json()).then(j=>{
    const lista=j.testes||j.arquivos||[]
    ul.innerHTML=''
    lista.forEach(t=>{
      const li=document.createElement('li')
      li.className='item-teste'
      const a=document.createElement('a')
      a.textContent=t.nome||t
      a.href=(t.url||('/api/download?execId='+encodeURIComponent(id)+'&nome='+encodeURIComponent(t.nome||t)))
      a.download=''
      const span=document.createElement('span')
      span.className='badge'
      span.textContent=(t.tipo||'JUnit')+(t.cobertura?' • '+t.cobertura:'')
      li.appendChild(a)
      li.appendChild(span)
      ul.appendChild(li)
    })
  })
  fetch('/api/explicacoes?execId='+encodeURIComponent(id)).then(r=>r.json()).then(j=>{
    const lista=j.explicacoes||j.itens||[]
    expl.innerHTML=''
    lista.forEach(x=>{
      const div=document.createElement('div')
      div.className='explicacao'
      const h=document.createElement('h3')
      h.textContent=x.titulo||'Explicação'
      const p=document.createElement('p')
      p.textContent=x.texto||x
      div.appendChild(h)
      div.appendChild(p)
      expl.appendChild(div)
    })
  })
  fetch('/api/dashboards?execId='+encodeURIComponent(id)).then(r=>r.json()).then(j=>{
    const lista=j.dashboards||j.itens||[]
    dash.innerHTML=''
    lista.forEach(d=>{
      const c=document.createElement('div')
      c.className='dash'
      if(d.iframe){const i=document.createElement('iframe');i.src=d.iframe;c.appendChild(i)}
      else if(d.imagem){const img=document.createElement('img');img.src=d.imagem;c.appendChild(img)}
      else if(typeof d==='string'){const i=document.createElement('iframe');i.src=d;c.appendChild(i)}
      dash.appendChild(c)
    })
  })
}
