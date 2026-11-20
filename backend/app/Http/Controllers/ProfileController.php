<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Validation\Rule;
use Illuminate\Support\Facades\Storage;

class ProfileController extends Controller
{
    public function show(Request $request)
    {
        return response()->json(['user' => $request->user()]);
    }

    public function uploadMedia(Request $request)
    {
        abort_unless(config('marketpg.profile_uploads_enabled'), 503, 'Profile photo uploads are disabled until persistent public image storage is configured.');
        $data = $request->validate([
            'profile_photo' => ['sometimes', 'required', 'image', 'mimes:jpg,jpeg,png,webp', 'max:5120'],
            'cover_photo' => ['sometimes', 'required', 'image', 'mimes:jpg,jpeg,png,webp', 'max:8192'],
        ]);
        abort_unless($request->hasFile('profile_photo') || $request->hasFile('cover_photo'), 422, 'Choose a profile photo or cover photo.');
        $user = $request->user();
        $disk = config('marketpg.public_disk');
        foreach (['profile_photo' => 'profile_photo_path', 'cover_photo' => 'cover_photo_path'] as $input => $column) {
            if ($request->hasFile($input)) {
                if ($user->{$column}) Storage::disk($disk)->delete($user->{$column});
                $user->{$column} = $request->file($input)->store('user-profiles', $disk);
            }
        }
        $user->save();
        return response()->json(['message' => 'Profile images updated.', 'user' => $user->fresh()]);
    }

    public function update(Request $request)
    {
        $data = $request->validate([
            'name' => ['required', 'string', 'max:120'],
            'phone' => ['nullable', 'string', 'max:40'],
            'location' => ['nullable', 'string', 'max:120'],
            'town' => ['nullable', 'string', 'max:100'],
            'province' => ['nullable', Rule::in(config('marketpg.png_provinces'))],
            'bank_name' => ['nullable', 'string', 'max:120'],
            'account_name' => ['nullable', 'string', 'max:120'],
            'account_number' => ['nullable', 'string', 'max:80'],
            'account_type' => ['required', Rule::in(['buyer', 'seller', 'both'])],
            'seller_bio' => ['nullable', 'string', 'max:2000'],
            'seller_website' => ['nullable', 'url', 'regex:/^https:\/\//i', 'max:2048'],
            'seller_facebook_url' => ['nullable', 'url', 'regex:/^https:\/\//i', 'max:2048'],
            'seller_x_url' => ['nullable', 'url', 'regex:/^https:\/\//i', 'max:2048'],
            'seller_linkedin_url' => ['nullable', 'url', 'regex:/^https:\/\//i', 'max:2048'],
            'sme_tin' => ['nullable', 'string', 'min:5', 'max:80'],
        ]);

        $bankFields = ['bank_name', 'account_name', 'account_number'];
        $providedBankFields = array_filter($bankFields, fn ($field) => !empty($data[$field]));
        if ($providedBankFields && count($providedBankFields) !== 3) {
            return response()->json(['message' => 'Provide bank name, account name and account number together.'], 422);
        }
        if (count($providedBankFields) === 3) {
            $data['payout_account_details'] = array_intersect_key($data, array_flip($bankFields));
        }
        foreach ($bankFields as $field) unset($data[$field]);

        if (!empty($data['province'])) {
            $data['location'] = trim(implode(', ', array_filter([$data['town'] ?? null, $data['province']])));
        }
        if (array_key_exists('sme_tin', $data)) {
            if (trim((string) $data['sme_tin']) !== '') {
                $data['sme_status'] = 'pending';
                $data['sme_review_note'] = null;
                $data['sme_reviewed_at'] = null;
            } else {
                $data['sme_tin'] = null;
                $data['sme_status'] = 'not_submitted';
                $data['sme_review_note'] = null;
                $data['sme_reviewed_at'] = null;
            }
        }
        $request->user()->update($data);

        return response()->json(['message' => 'Profile updated.', 'user' => $request->user()->fresh()]);
    }
}
