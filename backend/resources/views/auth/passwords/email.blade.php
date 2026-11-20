<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Reset password — MarketPG><script src="https://cdn.tailwindcss.com"></script></head>
<body class="min-h-screen bg-gray-50 text-gray-900 grid place-items-center p-4">
<main class="w-full max-w-md rounded-2xl bg-white p-7 shadow-sm">
<a href="/" class="text-xl font-semibold">Market<span class="text-blue-600">PG</span></a>
<h1 class="mt-8 text-2xl font-semibold">Forgot your password?</h1>
<p class="mt-2 text-sm text-gray-500">Enter your account email and we'll send you a time-limited password reset link.</p>
@if (session('status'))<div class="mt-4 rounded-lg bg-green-50 p-3 text-sm text-green-800">{{ session('status') }}</div>@endif
@if ($errors->any())<div class="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{{ $errors->first() }}</div>@endif
<form method="POST" action="{{ route('password.email') }}" class="mt-6 space-y-4">
@csrf
<label class="block text-sm font-medium">Email address<input name="email" type="email" required autocomplete="email" value="{{ old('email') }}" class="mt-2 w-full rounded-full bg-gray-100 px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"></label>
<button class="w-full rounded-full bg-blue-600 px-4 py-3 font-semibold text-white">Email me a reset link</button>
</form>
<a href="{{ route('login') }}" class="mt-5 block text-center text-sm text-gray-500">Back to sign in</a>
</main></body></html>
