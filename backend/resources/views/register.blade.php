<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Create account — MarketPG</title><script src="https://cdn.tailwindcss.com"></script>
<style>.field{width:100%;border:0;border-radius:9999px;background:#f3f4f6;padding:.85rem 1rem;outline:none}.choice{border:0;border-radius:14px;background:#f3f4f6;padding:12px;display:flex;align-items:center;gap:8px;cursor:pointer}.choice:has(input:checked){background:#dbeafe;color:#1d4ed8}.market-button{border-radius:9999px}</style>
<script>(function(){try{var t=localStorage.getItem('marketpg_theme')||(matchMedia('(prefers-color-scheme: dark)').matches?'dark':'light');document.documentElement.dataset.theme=t}catch(e){}})();</script>
<style>html[data-theme="dark"] body{background:#0b0d12;color:#f3f4f6}html[data-theme="dark"] main{background:#171a22}html[data-theme="dark"] .field,html[data-theme="dark"] .choice{background:#242936;color:#f3f4f6}html[data-theme="dark"] .choice:has(input:checked){background:#172554;color:#bfdbfe}</style></head>
<body class="grid min-h-screen place-items-center bg-gray-50 p-4"><main class="w-full max-w-md rounded-2xl bg-white p-7 shadow-sm">
<a href="/" class="text-xl font-semibold">Market<span class="text-blue-600">PG</span></a><h1 class="mt-7 text-2xl font-semibold">Create your account</h1><p class="mt-2 text-sm text-gray-500">Join the MarketPG community.</p>
<div id="error" class="mt-4 hidden rounded-lg bg-red-50 p-3 text-sm text-red-700"></div>
<form id="registerForm" class="mt-6 space-y-4">
<label class="block text-sm font-medium">Full name<input class="field mt-2" name="name" required maxlength="120" autocomplete="name"></label>
<label class="block text-sm font-medium">Email address<input class="field mt-2" name="email" type="email" required autocomplete="email"></label>
<div><p class="mb-2 text-sm font-medium">I want to join as a</p><div class="grid grid-cols-2 gap-3"><label class="choice"><input type="radio" name="account_type" value="buyer" checked> Buyer</label><label class="choice"><input type="radio" name="account_type" value="seller"> Seller</label></div></div>
<div class="rounded-xl bg-blue-50 p-3 text-sm text-blue-900">Please use your own genuine profile photo and cover picture. Fake, misleading or impersonated images may result in your account being suspended.</div>
<label class="block text-sm font-medium">Password<input class="field mt-2" name="password" type="password" minlength="10" required autocomplete="new-password"></label>
<label class="block text-sm font-medium">Confirm password<input class="field mt-2" name="password_confirmation" type="password" minlength="10" required autocomplete="new-password"></label>
<button id="submit" class="market-button w-full bg-blue-600 px-4 py-3 font-semibold text-white">Create account</button>
</form>
<p class="mt-5 text-center text-sm text-gray-500">Already have an account? <a href="/login" class="font-medium text-blue-600">Sign in</a></p>
</main>
<script>document.getElementById('registerForm').addEventListener('submit',async e=>{e.preventDefault();const btn=document.getElementById('submit'),err=document.getElementById('error'),f=new FormData(e.target);btn.disabled=true;btn.textContent='Creating account…';err.classList.add('hidden');try{const res=await fetch('/api/auth/register',{method:'POST',headers:{'Content-Type':'application/json','Accept':'application/json'},credentials:'omit',cache:'no-store',body:JSON.stringify({name:f.get('name'),email:f.get('email'),password:f.get('password'),password_confirmation:f.get('password_confirmation'),account_type:f.get('account_type')})});const data=await res.json();if(!res.ok)throw new Error(data.message||Object.values(data.errors||{}).flat().join(' ')||'Unable to create account.');sessionStorage.setItem('marketpg_token',data.token);sessionStorage.setItem('marketpg_user',JSON.stringify(data.user));location.href='/'}catch(ex){err.textContent=ex.message;err.classList.remove('hidden')}finally{btn.disabled=false;btn.textContent='Create account'}})</script>
</body></html>
