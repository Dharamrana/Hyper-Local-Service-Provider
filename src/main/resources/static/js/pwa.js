// HLSP PWA: register SW + friendly install prompt (Android beforeinstallprompt; iOS hint).
(function () {
  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("/sw.js").catch((e) => console.warn("SW failed", e));
    });
  }
  let deferred = null;
  window.addEventListener("beforeinstallprompt", (e) => {
    e.preventDefault(); deferred = e; showBar();
  });
  function showBar() {
    if (document.getElementById("hlspInstallBar")) return;
    const bar = document.createElement("div");
    bar.id = "hlspInstallBar";
    bar.style.cssText = "position:fixed;left:12px;right:12px;bottom:12px;z-index:9999;background:#14213d;color:#fff;"
      + "padding:12px 14px;border-radius:14px;display:flex;gap:10px;align-items:center;box-shadow:0 8px 30px rgba(0,0,0,.25)";
    bar.innerHTML = '<div style="flex:1;font-size:14px;line-height:1.35"><strong>Install HLSP app</strong><br>'
      + '<span style="opacity:.85">Faster booking, works on slow network.</span></div>';
    const btn = document.createElement("button");
    btn.textContent = "Install";
    btn.style.cssText = "background:#0f6bff;color:#fff;border:0;padding:10px 16px;border-radius:10px;font-weight:700";
    btn.onclick = async () => { bar.remove(); if (deferred) { deferred.prompt(); await deferred.userChoice; deferred = null; } };
    const close = document.createElement("button");
    close.textContent = "✕"; close.setAttribute("aria-label", "Dismiss");
    close.style.cssText = "background:transparent;color:#fff;border:0;font-size:16px;padding:8px";
    close.onclick = () => bar.remove();
    bar.append(btn, close); document.body.appendChild(bar);
  }
  // iOS Safari: no beforeinstallprompt, show hint once
  const isIOS = /iphone|ipad|ipod/i.test(navigator.userAgent);
  const standalone = window.matchMedia("(display-mode: standalone)").matches || window.navigator.standalone;
  if (isIOS && !standalone && !sessionStorage.getItem("hlspIosHint")) {
    sessionStorage.setItem("hlspIosHint", "1");
    setTimeout(() => {
      const d = document.createElement("div");
      d.style.cssText = "position:fixed;left:12px;right:12px;bottom:12px;z-index:9999;background:#fff;color:#14213d;"
        + "border:1px solid #e6e9f2;padding:12px 14px;border-radius:14px;font-size:14px;box-shadow:0 8px 30px rgba(0,0,0,.15)";
      d.innerHTML = "Install HLSP: tap <strong>Share</strong> → <strong>Add to Home Screen</strong>. <span style='float:right;cursor:pointer'>✕</span>";
      d.onclick = () => d.remove(); document.body.appendChild(d);
    }, 2500);
  }
})();
