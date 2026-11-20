<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>@yield('title') — MarketPG</title><script src="https://cdn.tailwindcss.com"></script><script>
(function(){try{var saved=localStorage.getItem('marketpg_theme');var theme=saved||(window.matchMedia&&window.matchMedia('(prefers-color-scheme: dark)').matches?'dark':'light');document.documentElement.dataset.theme=theme;}catch(e){document.documentElement.dataset.theme='light';}})();
</script>
<style>
.market-theme-toggle{display:inline-flex;align-items:center;justify-content:center;gap:.45rem;min-width:42px;height:40px;padding:0 .8rem;border:0;border-radius:9999px;background:#f3f4f6;color:#374151;font-size:14px;font-weight:600;cursor:pointer;transition:background .2s,color .2s}
.market-theme-toggle:hover{background:#e5e7eb}
html[data-theme="dark"] body{background:#0b0d12!important;color:#e5e7eb!important}
html[data-theme="dark"] .bg-white{background-color:#171a22!important}
html[data-theme="dark"] .bg-gray-50{background-color:#0b0d12!important}
html[data-theme="dark"] .bg-gray-100{background-color:#222631!important}
html[data-theme="dark"] .bg-gray-200{background-color:#303644!important}
html[data-theme="dark"] .bg-gray-800{background-color:#252b36!important}
html[data-theme="dark"] .text-gray-900,html[data-theme="dark"] .text-gray-800,html[data-theme="dark"] .text-gray-700{color:#f3f4f6!important}
html[data-theme="dark"] .text-gray-600,html[data-theme="dark"] .text-gray-500{color:#a1a1aa!important}
html[data-theme="dark"] .text-gray-400{color:#8b93a3!important}
html[data-theme="dark"] .border-gray-100,html[data-theme="dark"] .border-gray-200,html[data-theme="dark"] .border-gray-300{border-color:#303644!important}
html[data-theme="dark"] .market-toolbar{background:rgba(17,20,27,.96)!important;box-shadow:0 1px 2px rgba(0,0,0,.35)}
html[data-theme="dark"] .market-card{background:#171a22!important;box-shadow:0 1px 3px rgba(0,0,0,.35)}
html[data-theme="dark"] .market-input{background:#222631!important;color:#f3f4f6!important}
html[data-theme="dark"] .market-input::placeholder{color:#8b93a3}
html[data-theme="dark"] .market-theme-toggle{background:#292f3b;color:#facc15}
html[data-theme="dark"] .market-theme-toggle:hover{background:#363e4c}
html[data-theme="dark"] .bg-lime-50{background:#202b16!important}
html[data-theme="dark"] .text-lime-800{color:#bef264!important}
html[data-theme="dark"] .text-lime-700{color:#a3e635!important}
html[data-theme="dark"] footer{background:#11141b!important;border-color:#303644!important}
html[data-theme="dark"] .prose{color:#cbd5e1}
html[data-theme="dark"] .prose h2{color:#f3f4f6}
html[data-theme="dark"] input,html[data-theme="dark"] textarea,html[data-theme="dark"] select{color:#f3f4f6}
@media(prefers-reduced-motion:reduce){.market-theme-toggle{transition:none}}
</style></head><body class="bg-gray-50 text-gray-800"><header class="border-b border-gray-200 bg-white"><div class="mx-auto flex max-w-4xl items-center justify-between px-4 py-3"><a href="/" class="text-xl font-medium">Market<span class="text-lime-600">PG</span></a><div class="flex items-center gap-2"><button type="button" data-marketpg-theme-toggle onclick="marketpgToggleTheme()" class="market-theme-toggle" aria-label="Toggle colour theme">☾ Dark</button><a href="/" class="text-sm text-gray-500 hover:text-gray-900">Marketplace</a></div></div></header><main class="mx-auto max-w-4xl px-4 py-10"><a href="/" class="text-sm text-gray-500">← Back to marketplace</a><article class="mt-5 rounded-2xl border border-gray-200 bg-white p-6 sm:p-10"><h1 class="text-3xl font-medium tracking-tight text-gray-900">@yield('heading')</h1><div class="prose prose-gray mt-7 max-w-none text-sm leading-7 [&_h2]:mb-2 [&_h2]:mt-7 [&_h2]:text-lg [&_h2]:font-semibold [&_h2]:text-gray-900 [&_li]:ml-5 [&_li]:list-disc [&_ol_li]:list-decimal [&_p]:mb-4">@yield('content')</div></article></main><footer class="border-t border-gray-200 bg-white py-5 text-center text-xs text-gray-400">© {{ date('Y') }} MarketPG · <a href="/privacy-policy">Privacy</a> · <a href="/terms-and-conditions">Terms</a></footer><script>
(function(){function setTheme(theme){document.documentElement.dataset.theme=theme;try{localStorage.setItem('marketpg_theme',theme);}catch(e){}document.querySelectorAll('[data-marketpg-theme-toggle]').forEach(function(button){button.textContent=theme==='dark'?'☀ Light':'☾ Dark';button.setAttribute('aria-label',theme==='dark'?'Switch to light mode':'Switch to dark mode');button.setAttribute('title',theme==='dark'?'Switch to light mode':'Switch to dark mode');});}
window.marketpgToggleTheme=function(){setTheme(document.documentElement.dataset.theme==='dark'?'light':'dark');};
document.addEventListener('DOMContentLoaded',function(){setTheme(document.documentElement.dataset.theme||'light');});
})();
</script></body></html>