<!doctype html>
<html lang="en"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>MarketPG — Shop local in Papua New Guinea</title>
<meta name="description" content="Discover local deals and shop new and pre-loved goods from sellers across Papua New Guinea.">
<script>tailwind={config:{theme:{extend:{colors:{brand:{400:'#a3e635',500:'#84cc16'}}}}}};</script><script src="https://cdn.tailwindcss.com"></script>
<link href="https://cdn.jsdelivr.net/npm/flowbite@2.5.2/dist/flowbite.min.css" rel="stylesheet">
<style>
.shimmer{position:relative;overflow:hidden;background:#e5e7eb}.shimmer:after{content:"";position:absolute;inset:0;transform:translateX(-100%);background:linear-gradient(90deg,transparent,rgba(255,255,255,.72),transparent);animation:shine 1.35s infinite}@keyframes shine{100%{transform:translateX(100%)}}.product-image{transition:transform .35s ease}.product-card:hover .product-image{transform:scale(1.045)}.line-clamp-2{display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}

/* MarketPG soft, borderless marketplace treatment */
.market-toolbar{box-shadow:0 1px 2px rgba(15,23,42,.08);border:0!important}
.market-card{border:0!important;box-shadow:0 1px 2px rgba(15,23,42,.08)}
.market-card:hover{box-shadow:0 2px 5px rgba(15,23,42,.10)}
.market-input{border:0!important;background:#f3f4f6!important;border-radius:9999px!important;box-shadow:none!important;outline:none!important}
.market-button{border-radius:9999px!important}
</style>
@if (($ads['enabled'] ?? false) && !empty($ads['web_publisher_id']) && !empty($ads['web_slot_id']))
<script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client={{ $ads['web_publisher_id'] }}" crossorigin="anonymous"></script>
@endif
<script>
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
</style>
</head>
<body class="bg-gray-50 text-gray-800 antialiased">
<header class="market-toolbar sticky top-0 z-40 bg-white/95 backdrop-blur">
 <div class="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3">
  <a href="/" class="flex shrink-0 items-center gap-2.5"><span class="grid h-9 w-9 place-items-center rounded-xl bg-lime-400 text-lg font-semibold text-gray-950">M</span><span class="text-xl font-medium tracking-tight text-gray-900">Market<span class="text-lime-600">PG</span></span></a>
  <form action="/" class="mx-auto hidden w-full max-w-2xl items-center rounded-full bg-gray-100 px-4 sm:flex"><svg class="h-5 w-5 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg><input name="q" value="{{ request('q') }}" placeholder="Search products, brands and more" class="market-input w-full px-3 py-2.5 text-sm"><button class="market-button px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200">Search</button></form>
  <nav class="ml-auto flex shrink-0 items-center gap-2"><button type="button" data-marketpg-theme-toggle onclick="marketpgToggleTheme()" class="market-theme-toggle" aria-label="Toggle colour theme">☾ Dark</button><a href="/about" class="hidden px-3 py-2 text-sm text-gray-600 hover:text-gray-950 md:inline">About</a><a href="/login" class="market-button bg-gray-100 px-4 py-2 text-sm font-medium hover:bg-gray-200">Sign in</a></nav>
 </div>
 <form action="/" class="mx-4 mb-3 flex items-center rounded-full bg-gray-100 px-4 sm:hidden"><input name="q" value="{{ request('q') }}" placeholder="Search the marketplace" class="market-input w-full px-2 py-2.5 text-sm"><button class="market-button px-3 py-2 text-sm font-medium">Search</button></form>
</header>
<main class="mx-auto max-w-7xl px-4 pb-16">
 <section class="mt-5 grid gap-5 overflow-hidden rounded-2xl bg-[#171b22] p-6 text-white md:grid-cols-[1.15fr_.85fr] md:p-10">
  <div class="flex flex-col justify-center"><p class="text-xs font-medium uppercase tracking-[.22em] text-lime-300">Shop local. Sell local.</p><h1 class="mt-3 max-w-xl text-3xl font-medium leading-tight tracking-tight sm:text-4xl">Good finds are closer than you think.</h1><p class="mt-3 max-w-lg text-sm leading-6 text-gray-300">Explore new and pre-loved items from people and businesses across Papua New Guinea.</p><div class="mt-6 flex flex-wrap gap-3"><a href="#products" class="market-button bg-lime-400 px-5 py-2.5 text-sm font-semibold text-gray-950 hover:bg-lime-300">Explore products</a><a href="/about" class="market-button border border-white/20 px-5 py-2.5 text-sm font-medium hover:bg-white/10">How MarketPG works</a></div></div>
  <div class="hidden grid-cols-2 gap-3 md:grid"><div class="overflow-hidden rounded-xl bg-gray-800"><img class="h-full min-h-48 w-full object-cover" src="https://images.unsplash.com/photo-1498049794561-7780e7231661?auto=format&fit=crop&w=700&q=80" alt="Electronics and gadgets"></div><div class="grid gap-3"><img class="h-28 w-full rounded-xl object-cover" src="https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=700&q=80" alt="Home furniture"><img class="h-28 w-full rounded-xl object-cover" src="https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=700&q=80" alt="Fashion and footwear"></div></div>
 </section>
 <section class="mt-7 flex flex-wrap gap-2"><a href="/" class="rounded-full border border-lime-300 bg-lime-50 px-4 py-2 text-sm font-medium text-lime-800">All items</a>@foreach(['Electronics','Phones & Tablets','Vehicles','Furniture','Home & Living','Fashion','Sports'] as $category)<a href="/?category={{ $loop->iteration }}" class="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm text-gray-600 hover:border-lime-400 hover:text-gray-900">{{ $category }}</a>@endforeach</section>
 @if (($ads['enabled'] ?? false) && !empty($ads['web_publisher_id']) && !empty($ads['web_slot_id']))
 <div class="mx-auto my-7 max-w-4xl rounded-xl border border-gray-100 bg-white p-3 text-center"><span class="mb-2 block text-[10px] uppercase tracking-widest text-gray-400">Advertisement</span><ins class="adsbygoogle" style="display:block;min-height:90px" data-ad-client="{{ $ads['web_publisher_id'] }}" data-ad-slot="{{ $ads['web_slot_id'] }}" data-ad-format="auto" data-full-width-responsive="true"></ins></div><script>(adsbygoogle = window.adsbygoogle || []).push({});</script>
 @endif
 <section class="mt-9">
 <div><p class="text-xs font-medium uppercase tracking-widest text-lime-700">Trusted by the community</p><h2 class="mt-1 text-xl font-medium tracking-tight text-gray-900">Top sellers</h2></div>
 <div class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
 @forelse($topSellers as $seller)
 <a href="{{ route('seller.show', $seller) }}" class="market-card flex items-center gap-3 rounded-xl bg-white p-3 transition hover:-translate-y-0.5">
 @if($seller->profile_photo_url)<img src="{{ $seller->profile_photo_url }}" alt="" class="h-11 w-11 shrink-0 rounded-full object-cover">@else<span class="grid h-11 w-11 shrink-0 place-items-center rounded-full bg-lime-100 font-medium text-lime-800">{{ strtoupper(substr($seller->name,0,1)) }}</span>@endif
 <div class="min-w-0"><p class="truncate text-sm font-medium text-gray-900">{{ $seller->name }}</p><p class="mt-1 text-xs text-gray-500">{{ $seller->completed_sales_count }} sales</p></div>
 </a>
 @empty<p class="col-span-full text-sm text-gray-500">Our seller community is growing. Check back soon.</p>@endforelse
 </div></section>
 <div id="products" class="mt-9 flex items-end justify-between gap-3"><div><p class="text-xs font-medium uppercase tracking-widest text-lime-700">Fresh on MarketPG</p><h2 class="mt-1 text-2xl font-medium tracking-tight text-gray-900">{{ request('q') ? 'Search results' : 'Discover products' }}</h2><p class="mt-1 text-sm text-gray-500">Find something you’ll love.</p></div><span class="text-xs text-gray-500">{{ $listings->total() }} products</span></div>
 <div class="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3 sm:gap-5 lg:grid-cols-4 xl:grid-cols-5">
 @forelse($listings as $item)
 <a href="{{ route('listing.show',$item) }}" class="market-card product-card group overflow-hidden rounded-xl bg-white transition hover:-translate-y-0.5">
  <div class="relative aspect-square overflow-hidden bg-gray-100 @if($item->image_url) shimmer @endif">@if($item->image_url)<img loading="lazy" src="{{ $item->image_url }}" alt="{{ $item->title }}" onload="this.parentElement.classList.remove('shimmer')" onerror="this.parentElement.classList.remove('shimmer')" class="product-image h-full w-full object-cover">@else<div class="grid h-full w-full place-items-center text-gray-300"><svg class="h-12 w-12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="m21 15-5-5L5 21"/></svg></div>@endif<span class="absolute left-2 top-2 rounded-md bg-white/90 px-2 py-1 text-[10px] font-medium text-gray-600">{{ ucfirst($item->condition ?? 'Item') }}</span></div>
  <div class="p-3"><h3 class="line-clamp-2 min-h-10 text-sm font-medium leading-5 text-gray-900">{{ $item->title }}</h3><p class="mt-2 text-lg font-semibold tracking-tight text-gray-900">K{{ number_format((float)$item->price,2) }}</p><p class="mt-1 truncate text-xs text-gray-500">{{ $item->location }}</p><div class="mt-3 flex items-center gap-2 border-t border-gray-100 pt-2.5">@if($item->user?->profile_photo_url)<img src="{{ $item->user->profile_photo_url }}" alt="" class="h-6 w-6 rounded-full object-cover">@else<span class="grid h-6 w-6 place-items-center rounded-full bg-gray-100 text-[10px]">{{ strtoupper(substr($item->user?->name ?? 'S',0,1)) }}</span>@endif<span class="truncate text-xs text-gray-500">{{ $item->user?->name ?? 'MarketPG seller' }}</span></div></div>
 </a>
 @empty
 <div class="market-card col-span-full rounded-xl bg-white px-6 py-16 text-center"><p class="text-base font-medium text-gray-800">No products found</p><p class="mt-1 text-sm text-gray-500">Try another search or come back soon for new listings.</p><a href="/" class="mt-4 inline-block text-sm font-medium text-lime-700">Clear search</a></div>
 @endforelse
 </div>
 <div class="mt-8">{{ $listings->links() }}</div>
</main>
<footer class="border-t border-gray-200 bg-white"><div class="mx-auto grid max-w-7xl gap-6 px-4 py-8 sm:grid-cols-2"><div><a href="/" class="text-lg font-medium text-gray-900">Market<span class="text-lime-600">PG</span></a><p class="mt-2 max-w-sm text-sm leading-6 text-gray-500">A local marketplace connecting buyers and sellers throughout Papua New Guinea.</p></div><div class="flex flex-wrap gap-x-6 gap-y-3 text-sm text-gray-500 sm:justify-end sm:content-start"><a href="/about" class="hover:text-gray-900">About</a><a href="/privacy-policy" class="hover:text-gray-900">Privacy policy</a><a href="/terms-and-conditions" class="hover:text-gray-900">Terms & conditions</a><a href="/login" class="hover:text-gray-900">Sign in</a></div></div><div class="border-t border-gray-100 py-4 text-center text-xs text-gray-400">© {{ date('Y') }} MarketPG. All rights reserved.</div></footer>
<script src="https://cdn.jsdelivr.net/npm/flowbite@2.5.2/dist/flowbite.min.js"></script><script>
(function(){function setTheme(theme){document.documentElement.dataset.theme=theme;try{localStorage.setItem('marketpg_theme',theme);}catch(e){}document.querySelectorAll('[data-marketpg-theme-toggle]').forEach(function(button){button.textContent=theme==='dark'?'☀ Light':'☾ Dark';button.setAttribute('aria-label',theme==='dark'?'Switch to light mode':'Switch to dark mode');button.setAttribute('title',theme==='dark'?'Switch to light mode':'Switch to dark mode');});}
window.marketpgToggleTheme=function(){setTheme(document.documentElement.dataset.theme==='dark'?'light':'dark');};
document.addEventListener('DOMContentLoaded',function(){setTheme(document.documentElement.dataset.theme||'light');});
})();
</script></body></html>