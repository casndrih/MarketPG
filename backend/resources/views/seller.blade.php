<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{{ $user->name }} — MarketPG seller</title><script src="https://cdn.tailwindcss.com"></script><style>.market-toolbar{box-shadow:0 1px 2px rgba(15,23,42,.08);border:0!important}.market-card{border:0!important;box-shadow:0 1px 2px rgba(15,23,42,.08)}.market-pill{border-radius:9999px!important}</style><script>
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
</style></head><body class="bg-gray-50 text-gray-800"><header class="market-toolbar bg-white"><div class="mx-auto flex max-w-6xl items-center justify-between px-4 py-3"><a href="/" class="text-xl font-medium">Market<span class="text-blue-600">PG</span></a><div class="flex items-center gap-2"><button type="button" data-marketpg-theme-toggle onclick="marketpgToggleTheme()" class="market-theme-toggle" aria-label="Toggle colour theme">☾ Dark</button><a href="/login" class="market-pill bg-gray-100 px-4 py-2 text-sm">Sign in</a></div></div></header><main class="mx-auto max-w-6xl px-4 py-7"><a href="/" class="text-sm text-gray-500">← Marketplace</a><section class="market-card mt-5 overflow-hidden rounded-2xl bg-white"><div class="h-40 bg-gray-800 sm:h-56">@if($user->cover_photo_url)<img src="{{ $user->cover_photo_url }}" class="h-full w-full object-cover" alt="Seller cover">@endif</div><div class="px-5 pb-6 sm:px-8">@if($user->profile_photo_url)<img src="{{ $user->profile_photo_url }}" class="-mt-12 h-24 w-24 rounded-full border-4 border-white bg-white object-cover" alt="{{ $user->name }}">@else<div class="-mt-12 grid h-24 w-24 place-items-center rounded-full border-4 border-white bg-blue-100 text-3xl">{{ strtoupper(substr($user->name,0,1)) }}</div>@endif<h1 class="mt-3 text-2xl font-medium text-gray-900">{{ $user->name }}</h1><p class="mt-1 text-sm text-gray-500">{{ $user->town ? $user->town.', ' : '' }}{{ $user->province ?? $user->location ?? 'Papua New Guinea' }}</p><div class="mt-4 flex flex-wrap gap-3 text-sm"><span class="rounded-full bg-blue-50 px-3 py-1 text-blue-800">{{ $user->seller_rank }} seller</span><span class="rounded-full bg-gray-100 px-3 py-1 text-gray-600">{{ $sales }} completed sales</span><span class="rounded-full bg-gray-100 px-3 py-1 text-gray-600">{{ $listings->total() }} active listings</span></div></div></section><section class="mt-9"><div class="flex items-center justify-between"><h2 class="text-xl font-medium text-gray-900">Seller's listings</h2><span class="text-sm text-gray-500">{{ $listings->total() }} items</span></div><div class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">@forelse($listings as $item)<a href="{{ route('listing.show',$item) }}" class="market-card overflow-hidden rounded-xl bg-white">@if($item->image_url)<img src="{{ $item->image_url }}" class="aspect-square w-full object-cover" alt="{{ $item->title }}">@else<div class="aspect-square bg-gray-100"></div>@endif<div class="p-3"><p class="line-clamp-2 min-h-10 text-sm">{{ $item->title }}</p><p class="mt-2 font-semibold">K{{ number_format((float)$item->price,2) }}</p><p class="mt-1 text-xs text-gray-500">{{ $item->location }}</p></div></a>@empty<p class="market-card col-span-full rounded-xl bg-white p-12 text-center text-sm text-gray-500">This seller has no active listings right now.</p>@endforelse</div><div class="mt-6">{{ $listings->links() }}</div></section></main><script>
(function(){function setTheme(theme){document.documentElement.dataset.theme=theme;try{localStorage.setItem('marketpg_theme',theme);}catch(e){}document.querySelectorAll('[data-marketpg-theme-toggle]').forEach(function(button){button.textContent=theme==='dark'?'☀ Light':'☾ Dark';button.setAttribute('aria-label',theme==='dark'?'Switch to light mode':'Switch to dark mode');button.setAttribute('title',theme==='dark'?'Switch to light mode':'Switch to dark mode');});}
window.marketpgToggleTheme=function(){setTheme(document.documentElement.dataset.theme==='dark'?'light':'dark');};
document.addEventListener('DOMContentLoaded',function(){setTheme(document.documentElement.dataset.theme||'light');});
})();
</script></body></html>