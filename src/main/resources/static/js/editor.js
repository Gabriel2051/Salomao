/* SALOMÃO — editor rico v2: blocos (títulos/citação/código), fontes e tamanhos,
   realce, tachado, sub/sup, lista, link, separador, modo foco e contador de
   palavras. Menções @Personagem com autocomplete. Tudo sanitizado no servidor. */
(function () {
  "use strict";

  function initEditor(root) {
    var area = root.querySelector("[data-editor-area]");
    var hidden = root.querySelector("[data-editor-hidden]");
    var mencionaUrl = root.getAttribute("data-menciona-url");
    if (!area || !hidden) return;
    area.innerHTML = hidden.value || "";

    function sync() {
      hidden.value = area.innerHTML;
      contar();
      // bubbles: o listener de autosave fica no <form data-autosave> (ancestral)
      root.dispatchEvent(new CustomEvent("editor:change", { bubbles: true }));
    }

    // ---------- preservação da seleção ----------
    // Botões de toolbar nunca roubam o foco do texto (mousedown preventDefault);
    // selects roubam — então o último range válido é memorizado e restaurado.
    var ultimaRange = null;
    document.addEventListener("selectionchange", function () {
      var sel = window.getSelection();
      if (sel.rangeCount && area.contains(sel.getRangeAt(0).commonAncestorContainer)) {
        ultimaRange = sel.getRangeAt(0).cloneRange();
        atualizarBotoes();
      }
    });
    function restaurarSelecao() {
      if (!ultimaRange) { area.focus(); return; }
      var sel = window.getSelection();
      sel.removeAllRanges();
      sel.addRange(ultimaRange);
    }
    root.querySelectorAll(".editor-barra button").forEach(function (b) {
      b.addEventListener("mousedown", function (e) { e.preventDefault(); });
    });

    // ---------- comandos nativos (bold, italic, listas…) ----------
    root.querySelectorAll("[data-cmd]").forEach(function (btn) {
      btn.addEventListener("click", function (e) {
        e.preventDefault();
        area.focus();
        var cmd = btn.getAttribute("data-cmd");
        var val = btn.getAttribute("data-valor") || null;
        if (cmd === "createLink") {
          var url = window.prompt("URL do link (https://…):", "https://");
          if (!url) return;
          url = url.trim();
          // bloqueia esquemas perigosos (javascript:, data:…) antes de inserir
          if (!/^(https?:\/\/|mailto:)/i.test(url)) {
            window.alert("Use apenas links http://, https:// ou mailto:.");
            return;
          }
          document.execCommand("createLink", false, url);
        } else if (cmd === "subscript" || cmd === "superscript") {
          // um exclui o outro: ligar sub desliga sup e vice-versa
          var outro = cmd === "subscript" ? "superscript" : "subscript";
          try { if (document.queryCommandState(outro)) document.execCommand(outro, false, null); } catch (err) {}
          document.execCommand(cmd, false, null);
        } else {
          document.execCommand(cmd, false, val);
        }
        sync();
        atualizarBotoes();
      });
    });

    // ---------- select de bloco (P / H1 / H2 / citação / código) ----------
    var selBloco = root.querySelector("[data-bloco]");
    if (selBloco) selBloco.addEventListener("change", function () {
      restaurarSelecao();
      document.execCommand("formatBlock", false, selBloco.value);
      selBloco.value = "p";
      sync();
    });

    // ---------- fonte, tamanho e cor via classes (span.ff-*/fs-*) ----------
    function familia(el, prefixo) {
      Array.prototype.slice.call(el.classList).forEach(function (c) {
        if (c.indexOf(prefixo) === 0) el.classList.remove(c);
      });
    }
    function desembrulhar(el) {
      var pai = el.parentNode;
      while (el.firstChild) pai.insertBefore(el.firstChild, el);
      pai.removeChild(el);
    }
    // envolve a seleção em <tag class>; se ela já estiver dentro de um elemento
    // dessa família, apenas troca a classe (não aninha spans desnecessariamente)
    function envolver(tag, classe, prefixo) {
      restaurarSelecao();
      var sel = window.getSelection();
      if (!sel.rangeCount) return;
      var range = sel.getRangeAt(0);
      if (range.collapsed || !area.contains(range.commonAncestorContainer)) return;
      var el = range.commonAncestorContainer.nodeType === 1
        ? range.commonAncestorContainer : range.commonAncestorContainer.parentElement;
      var alvo = el && el.closest ? el.closest(tag) : null;
      if (alvo && area.contains(alvo) && alvo !== area
          && alvo.contains(range.startContainer) && alvo.contains(range.endContainer)) {
        if (tag === "mark") { desembrulhar(alvo); sync(); return; }
        if (prefixo) familia(alvo, prefixo);
        if (classe) alvo.classList.add(classe);
        if (!alvo.getAttribute("class") && alvo.className !== "mencao") desembrulhar(alvo);
        sync();
        return;
      }
      if (!classe) return;
      var span = document.createElement(tag);
      span.className = classe;
      try {
        range.surroundContents(span);
      } catch (e) {
        span.appendChild(range.extractContents());
        range.insertNode(span);
      }
      window.getSelection().removeAllRanges();
      sync();
    }
    var selFonte = root.querySelector("[data-fonte]");
    if (selFonte) selFonte.addEventListener("change", function () {
      envolver("span", selFonte.value, "ff-"); selFonte.value = "";
    });
    var selTam = root.querySelector("[data-tamanho]");
    if (selTam) selTam.addEventListener("change", function () {
      envolver("span", selTam.value, "fs-"); selTam.value = "";
    });
    var btnMark = root.querySelector("[data-marcar]");
    if (btnMark) btnMark.addEventListener("click", function (e) {
      e.preventDefault(); envolver("mark", "", null);
    });

    // ---------- modo foco (tela cheia) ----------
    var btnExp = root.querySelector("[data-expandir]");
    function alternarFogo() {
      var on = root.classList.toggle("modo-total");
      document.body.classList.toggle("editor-foco", on);
      if (btnExp) btnExp.classList.toggle("ativo", on);
      area.focus();
    }
    if (btnExp) btnExp.addEventListener("click", function (e) { e.preventDefault(); alternarFogo(); });
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && root.classList.contains("modo-total")) alternarFogo();
    });

    // ---------- estado visual dos botões ----------
    document.addEventListener("selectionchange", function () {
      if (document.activeElement === area) atualizarBotoes();
    });
    function atualizarBotoes() {
      root.querySelectorAll("[data-cmd]").forEach(function (btn) {
        try {
          var cmd = btn.getAttribute("data-cmd");
          if (["bold", "italic", "underline", "strikeThrough", "insertUnorderedList",
               "insertOrderedList", "subscript", "superscript"].indexOf(cmd) >= 0) {
            btn.classList.toggle("ativo", document.queryCommandState(cmd));
          }
        } catch (err) { /* noop */ }
      });
    }

    // ---------- contador de palavras ----------
    var contador = root.querySelector("[data-contador]");
    function contar() {
      if (!contador) return;
      var texto = (area.textContent || "").trim();
      var palavras = texto ? texto.split(/\s+/).length : 0;
      contador.textContent = palavras + (palavras === 1 ? " palavra" : " palavras")
        + " · " + texto.length + " caracteres";
    }
    contar();

    ["input", "keyup", "mouseup"].forEach(function (ev) {
      area.addEventListener(ev, function () { sync(); });
    });
    // cola SEMPRE como texto puro com quebras de linha: evita XSS persistido
    // (o servidor sanitiza de novo, mas o editor nunca recebe HTML arbitrario)
    area.addEventListener("paste", function (e) {
      if (!e.clipboardData) return;
      var texto = e.clipboardData.getData("text/plain");
      if (!texto) { e.preventDefault(); return; }
      e.preventDefault();
      var seguro = texto
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(/\r\n|\r|\n/g, "<br>");
      document.execCommand("insertHTML", false, seguro);
      sync();
    });

    // Ctrl+S salva o formulário hospedeiro (padrão de editores profissionais)
    area.addEventListener("keydown", function (e) {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "s") {
        e.preventDefault();
        var form = root.closest("form");
        if (form) form.requestSubmit();
      }
    });

    // garante submit do form com conteúdo atual
    var form = root.closest("form");
    if (form) form.addEventListener("submit", sync);

    // Menções vindas do banco perdem os listeners ao recarregar o HTML:
    // delegação garante que TODA menção (nova ou recarregada) seja clicável.
    area.addEventListener("click", function (e) {
      var m = e.target.closest ? e.target.closest(".mencao[data-mention-id]") : null;
      if (m) window.location.href = "/personagens/" + m.getAttribute("data-mention-id");
    });

    // ---- menções @ ----
    if (mencionaUrl) initMencoes(root, area, mencionaUrl, sync);
  }

  function initMencoes(root, area, url, sync) {
    var box = document.createElement("ul");
    box.className = "autocomplete";
    box.hidden = true;
    box.setAttribute("role", "listbox");
    document.body.appendChild(box);
    var resultados = [];
    var indice = 0;
    var termo = null;

    area.addEventListener("keyup", function () {
      var sel = window.getSelection();
      if (!sel.rangeCount) { box.hidden = true; return; }
      var range = sel.getRangeAt(0);
      var antes = range.startContainer.textContent
        ? range.startContainer.textContent.slice(0, range.startOffset) : "";
      var m = antes.match(/@([\p{L}\p{N} ]{1,30})$/u);
      if (!m || !area.contains(range.startContainer)) { box.hidden = true; return; }
      termo = { texto: m[1], range: range.cloneRange() };
      buscar(m[1]);
    });

    var deb;
    function buscar(q) {
      clearTimeout(deb);
      deb = setTimeout(function () {
        window.salomaoFetch(url + "?q=" + encodeURIComponent(q)).then(function (lista) {
          resultados = lista || []; indice = 0;
          if (!resultados.length) { box.hidden = true; return; }
          render();
        }).catch(function () { box.hidden = true; });
      }, 180);
    }

    function render() {
      var r = termo.range.getBoundingClientRect();
      box.style.left = Math.min(window.innerWidth - 260, r.left + window.scrollX) + "px";
      box.style.top = (r.bottom + window.scrollY + 6) + "px";
      box.innerHTML = "";
      resultados.forEach(function (c, i) {
        var li = document.createElement("li");
        li.textContent = c.nome;
        li.setAttribute("role", "option");
        if (i === indice) li.classList.add("ativo");
        li.addEventListener("mousedown", function (e) { e.preventDefault(); aplicar(c); });
        box.appendChild(li);
      });
      box.hidden = false;
    }

    area.addEventListener("keydown", function (e) {
      if (box.hidden) return;
      if (e.key === "ArrowDown") { e.preventDefault(); indice = Math.min(indice + 1, resultados.length - 1); render(); }
      else if (e.key === "ArrowUp") { e.preventDefault(); indice = Math.max(indice - 1, 0); render(); }
      else if (e.key === "Enter" && resultados[indice]) { e.preventDefault(); aplicar(resultados[indice]); }
      else if (e.key === "Escape") { box.hidden = true; }
    });

    function aplicar(c) {
      box.hidden = true;
      var sel = window.getSelection();
      sel.removeAllRanges();
      sel.addRange(termo.range);
      // apaga o "@termo" digitado
      for (var i = 0; i < termo.texto.length + 1; i++) {
        document.execCommand("delete", false, null);
      }
      var span = document.createElement("span");
      span.className = "mencao";
      span.setAttribute("data-mention-id", c.id);
      span.setAttribute("data-mention-type", "CHARACTER");
      span.textContent = "@" + c.nome;
      span.title = "Abrir personagem (clique)";
      var range = sel.getRangeAt(0);
      range.insertNode(span);
      range.setStartAfter(span);
      range.insertNode(document.createTextNode(" "));
      range.collapse(false);
      sel.removeAllRanges();
      sel.addRange(range);
      // registra id para o backend persistir story_character_mentions
      var ids = root.querySelector("[data-mencoes-ids]");
      if (ids) {
        var atuais = (ids.value || "").split(",").filter(function (x) { return x; });
        if (atuais.indexOf(c.id) < 0) { atuais.push(c.id); ids.value = atuais.join(","); }
      }
      sync();
      area.focus();
    }
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-editor]").forEach(initEditor);
  });
})();
