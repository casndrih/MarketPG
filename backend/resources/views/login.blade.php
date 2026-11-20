<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Sign in — MarketPG</title><script src="https://cdn.tailwindcss.com"></script><style>.market-input{border:0!important;background:#f3f4f6!important;border-radius:9999px!important;box-shadow:none!important;outline:none!important}.market-button{border-radius:9999px!important}.market-card{border:0!important;box-shadow:0 1px 2px rgba(15,23,42,.08)}</style><script>
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
html[data-theme="dark"] .bg-blue-50{background:#172554!important}
html[data-theme="dark"] .text-blue-800{color:#bfdbfe!important}
html[data-theme="dark"] .text-blue-700{color:#60a5fa!important}
html[data-theme="dark"] footer{background:#11141b!important;border-color:#303644!important}
html[data-theme="dark"] .prose{color:#cbd5e1}
html[data-theme="dark"] .prose h2{color:#f3f4f6}
html[data-theme="dark"] input,html[data-theme="dark"] textarea,html[data-theme="dark"] select{color:#f3f4f6}
@media(prefers-reduced-motion:reduce){.market-theme-toggle{transition:none}}
</style></head><body class="grid min-h-screen place-items-center bg-gray-50 p-4"><button type="button" data-marketpg-theme-toggle onclick="marketpgToggleTheme()" class="market-theme-toggle fixed right-4 top-4" aria-label="Toggle colour theme">☾ Dark</button><main class="market-card w-full max-w-md rounded-2xl bg-white p-7"><a href="/" class="text-xl font-medium text-gray-900">Market<span class="text-blue-600">PG</span></a><h1 class="mt-8 text-2xl font-medium text-gray-900">Welcome back</h1><p class="mt-2 text-sm text-gray-500">Sign in to continue shopping on MarketPG.</p><div id="error" class="mt-4 hidden rounded-lg bg-red-50 p-3 text-sm text-red-700"></div><form id="loginForm" class="mt-6 space-y-4"><div><label class="mb-1.5 block text-sm font-medium">Email address</label><input name="email" type="email" required autocomplete="email" class="market-input w-full px-4 py-3 text-sm"></div><div><label class="mb-1.5 block text-sm font-medium">Password</label><input name="password" type="password" required autocomplete="current-password" class="w-full rounded-lg border border-gray-300 px-3 py-3 text-sm outline-none focus:border-blue-500"></div><button id="submit" class="market-button w-full bg-blue-400 px-4 py-3 text-sm font-semibold text-gray-950 hover:bg-blue-300">Sign in</button></form><p class="mt-5 text-center text-xs text-gray-400">New to MarketPG? Account registration is available in the mobile app.</p><a href="/" class="mt-5 block text-center text-sm text-gray-500 hover:text-gray-900">← Back to marketplace</a></main><script>document.getElementById('loginForm').addEventListener('submit',async e=>{e.preventDefault();const btn=document.getElementById('submit'),err=document.getElementById('error');btn.disabled=true;btn.textContent='Signing in…';err.classList.add('hidden');try{const f=new FormData(e.target);const res=await fetch('/api/auth/login',{method:'POST',headers:{'Content-Type':'application/json','Accept':'application/json'},body:JSON.stringify({email:f.get('email'),password:f.get('password')})});const data=await res.json();if(!res.ok)throw new Error(data.message||'Unable to sign in.');sessionStorage.setItem('marketpg_token',data.token);sessionStorage.setItem('marketpg_user',JSON.stringify(data.user));location.href='/'}catch(ex){err.textContent=ex.message;err.classList.remove('hidden')}finally{btn.disabled=false;btn.textContent='Sign in'}})</script><script>
(function(){function setTheme(theme){document.documentElement.dataset.theme=theme;try{localStorage.setItem('marketpg_theme',theme);}catch(e){}document.querySelectorAll('[data-marketpg-theme-toggle]').forEach(function(button){button.textContent=theme==='dark'?'☀ Light':'☾ Dark';button.setAttribute('aria-label',theme==='dark'?'Switch to light mode':'Switch to dark mode');button.setAttribute('title',theme==='dark'?'Switch to light mode':'Switch to dark mode');});}
window.marketpgToggleTheme=function(){setTheme(document.documentElement.dataset.theme==='dark'?'light':'dark');};
document.addEventListener('DOMContentLoaded',function(){setTheme(document.documentElement.dataset.theme||'light');});
})();
</script></body></html>