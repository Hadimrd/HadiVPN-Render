const CACHE_NAME = "hadivpn-pages-v1";
const APP_SHELL = ["./", "./index.html", "./manifest.webmanifest", "./assets/icon.svg"];
self.addEventListener("install", event => {
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(APP_SHELL)).then(() => self.skipWaiting()));
});
self.addEventListener("activate", event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener("fetch", event => {
  const request = event.request;
  if (request.method !== "GET") return;
  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;
  if (url.pathname.includes("/feeds/")) {
    event.respondWith(fetch(request).then(response => {
      if (response.ok) caches.open(CACHE_NAME).then(cache => cache.put(request, response.clone()));
      return response;
    }).catch(() => caches.match(request).then(cached => cached || new Response("اشتراک ذخیره‌شده در دسترس نیست؛ برای به‌روزرسانی اینترنت را بررسی کن.", {status:503, headers:{"Content-Type":"text/plain; charset=utf-8"}}))));
    return;
  }
  event.respondWith(fetch(request).then(response => {
    if (response.ok && request.mode === "navigate") caches.open(CACHE_NAME).then(cache => cache.put("./index.html", response.clone()));
    return response;
  }).catch(() => caches.match(request).then(cached => cached || caches.match("./index.html"))));
});