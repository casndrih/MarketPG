<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>MarketPG Admin Sign In</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="min-h-screen bg-slate-950 text-white flex items-center justify-center p-5">
    <main class="w-full max-w-md rounded-3xl border border-slate-800 bg-slate-900 p-8 shadow-xl">
        <a href="/" class="text-2xl font-black text-lime-400">MarketPG</a>
        <p class="mt-1 text-sm text-slate-400">Administrator access</p>
        <h1 class="mt-8 text-2xl font-bold">Sign in</h1>
        <p class="mt-2 text-sm text-slate-400">Use an account whose email is on the MarketPG administrator allowlist.</p>
        @if ($errors->any())
            <div class="mt-5 rounded-xl border border-red-900 bg-red-950/60 p-3 text-sm text-red-200">{{ $errors->first() }}</div>
        @endif
        <form method="POST" action="{{ route('admin.login.submit') }}" class="mt-6 space-y-4">
            @csrf
            <label class="block text-sm font-medium">Email
                <input name="email" type="email" required value="{{ old('email') }}" autocomplete="username" class="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none focus:border-lime-400">
            </label>
            <label class="block text-sm font-medium">Password
                <input name="password" type="password" required autocomplete="current-password" class="mt-2 w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none focus:border-lime-400">
            </label>
            <button class="w-full rounded-xl bg-lime-400 px-4 py-3 font-bold text-slate-950">Sign in to admin</button>
        </form>
        <a href="/" class="mt-6 inline-block text-sm text-slate-400 hover:text-white">Return to marketplace</a>
    </main>
</body>
</html>
