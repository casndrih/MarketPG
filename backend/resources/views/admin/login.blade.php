<!doctype html>
<html lang="en">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>MarketPG Admin Sign In</title><script>tailwind={config:{darkMode:'class',theme:{extend:{colors:{brand:{400:'#60a5fa',500:'#3b82f6'}}}}}};</script><script src="https://cdn.tailwindcss.com"></script><link href="https://cdn.jsdelivr.net/npm/flowbite@2.5.2/dist/flowbite.min.css" rel="stylesheet"><script>(function(){try{if(localStorage.getItem('marketpg_theme')==='dark')document.documentElement.classList.add('dark');}catch(e){}})();</script>
<style>
.admin-theme-toggle{display:inline-flex;align-items:center;justify-content:center;min-width:42px;height:38px;padding:0 .8rem;border:0;border-radius:9999px;background:#f3f4f6;color:#374151;font-size:13px;font-weight:600}
.dark .admin-theme-toggle{background:#292f3b;color:#facc15}
main{border:0!important}
input:not([type=checkbox]):not([type=radio]):not([type=file]),textarea,select{border:0!important;border-radius:9999px!important;box-shadow:none!important;outline:none;transition:background-color .18s ease,color .18s ease}
body:not(.dark) input:not([type=checkbox]):not([type=radio]):not([type=file]),body:not(.dark) textarea,body:not(.dark) select{background:#f1f3f6!important;color:#111827!important}
html.dark,html.dark body{background:#000!important;color:#f3f4f6!important}
html.dark main{background:#171a22!important;box-shadow:0 8px 32px rgba(0,0,0,.28)!important}
html.dark input:not([type=checkbox]):not([type=radio]):not([type=file]),html.dark textarea,html.dark select{background:#222631!important;color:#f3f4f6!important}
input::placeholder,textarea::placeholder{color:#929baa!important}
button[type=submit]{border:0!important;border-radius:9999px!important}
a:focus-visible,button:focus-visible,input:focus-visible{outline:2px solid #60a5fa;outline-offset:2px}

</style></head>
<body class="flex min-h-screen items-center justify-center bg-gray-50 p-5 dark:bg-gray-950">
<button type="button" data-admin-theme-toggle onclick="marketpgToggleAdminTheme()" class="admin-theme-toggle fixed right-4 top-4">☾ Dark</button><main class="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-8 shadow-sm dark:border-gray-800 dark:bg-gray-900">
    <a href="/" class="flex items-center gap-3"><span class="grid h-11 w-11 place-items-center rounded-xl bg-blue-400 text-xl font-black text-gray-950">M</span><span class="text-2xl font-extrabold text-gray-900 dark:text-white">Market<span class="text-blue-600 dark:text-blue-400">PG</span></span></a>
    <div class="mt-8"><span class="rounded-full bg-blue-100 px-3 py-1 text-xs font-bold uppercase tracking-wider text-blue-800 dark:bg-blue-950 dark:text-blue-300">Secure admin portal</span><h1 class="mt-4 text-2xl font-extrabold text-gray-900 dark:text-white">Welcome back</h1><p class="mt-2 text-sm leading-6 text-gray-500 dark:text-gray-400">Sign in with an account on the MarketPG administrator allowlist.</p></div>
    @if ($errors->any())<div class="mt-5 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800 dark:border-red-900 dark:bg-red-950/40 dark:text-red-300" role="alert">{{ $errors->first() }}</div>@endif
    <form method="POST" action="{{ route('admin.login.submit') }}" class="mt-6 space-y-5">@csrf
        <div><label for="email" class="mb-2 block text-sm font-medium text-gray-900 dark:text-white">Email address</label><input id="email" name="email" type="email" required value="{{ old('email') }}" autocomplete="username" class="block w-full rounded-lg border border-gray-300 bg-gray-50 p-3 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500 dark:border-gray-700 dark:bg-gray-800 dark:text-white" placeholder="admin@example.com"></div>
        <div><label for="password" class="mb-2 block text-sm font-medium text-gray-900 dark:text-white">Password</label><input id="password" name="password" type="password" required autocomplete="current-password" class="block w-full rounded-lg border border-gray-300 bg-gray-50 p-3 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500 dark:border-gray-700 dark:bg-gray-800 dark:text-white" placeholder="••••••••"></div>
        <button type="submit" class="w-full rounded-lg bg-blue-400 px-5 py-3 text-center text-sm font-bold text-gray-950 transition hover:bg-blue-300 focus:outline-none focus:ring-4 focus:ring-blue-200">Sign in to admin</button>
    </form>
    <div class="mt-6 border-t border-gray-100 pt-5 text-center dark:border-gray-800"><a href="/" class="text-sm font-medium text-gray-500 hover:text-blue-700 dark:text-gray-400 dark:hover:text-blue-400">← Return to marketplace</a></div>
</main><script src="https://cdn.jsdelivr.net/npm/flowbite@2.5.2/dist/flowbite.min.js"></script><script>window.marketpgToggleAdminTheme=function(){var dark=document.documentElement.classList.toggle('dark');try{localStorage.setItem('marketpg_theme',dark?'dark':'light');}catch(e){}document.querySelectorAll('[data-admin-theme-toggle]').forEach(function(b){b.textContent=dark?'☀ Light':'☾ Dark';});};document.addEventListener('DOMContentLoaded',function(){var dark=document.documentElement.classList.contains('dark');document.querySelectorAll('[data-admin-theme-toggle]').forEach(function(b){b.textContent=dark?'☀ Light':'☾ Dark';});});</script></body></html>
