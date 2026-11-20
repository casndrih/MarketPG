<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Choose a new password — MarketPG</title><script src="https://cdn.tailwindcss.com"></script></head>
<body class="min-h-screen bg-gray-50 text-gray-900 grid place-items-center p-4">
<main class="w-full max-w-md rounded-2xl bg-white p-7 shadow-sm">
<a href="/" class="text-xl font-semibold">Market<span class="text-blue-600">PG</span></a>
<h1 class="mt-8 text-2xl font-semibold">Choose a new password</h1>
<p class="mt-2 text-sm text-gray-500">Your reset link is time-limited. Choose a password with at least 10 characters, including letters and numbers.</p>
@if ($errors->any())<div class="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{{ $errors->first() }}</div>@endif
<form method="POST" action="{{ route('password.update') }}" class="mt-6 space-y-4">
@csrf
<input type="hidden" name="token" value="{{ $token }}">
<label class="block text-sm font-medium">Email address<input name="email" type="email" required autocomplete="email" value="{{ old('email', $email) }}" class="mt-2 w-full rounded-full bg-gray-100 px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"></label>
<label class="block text-sm font-medium">New password<input name="password" type="password" required autocomplete="new-password" class="mt-2 w-full rounded-full bg-gray-100 px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"></label>
<label class="block text-sm font-medium">Confirm new password<input name="password_confirmation" type="password" required autocomplete="new-password" class="mt-2 w-full rounded-full bg-gray-100 px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"></label>
<button class="w-full rounded-full bg-blue-600 px-4 py-3 font-semibold text-white">Reset password</button>
</form>
</main></body></html>
