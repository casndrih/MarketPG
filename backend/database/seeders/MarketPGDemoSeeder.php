<?php

namespace Database\Seeders;

use App\Models\Listing;
use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use RuntimeException;

class MarketPGDemoSeeder extends Seeder
{
    public function run(): void
    {
        $accounts = [
            ['name' => 'MarketPG Test Buyer', 'email' => 'buyer.test@marketpg.test', 'password' => env('MARKETPG_DEMO_BUYER_PASSWORD', 'Buyer-MPG-Test-2026!'), 'location' => 'Port Moresby', 'account_type' => 'buyer'],
            ['name' => 'MarketPG Test Seller', 'email' => 'seller.test@marketpg.test', 'password' => env('MARKETPG_DEMO_SELLER_PASSWORD', 'Seller-MPG-Test-2026!'), 'location' => 'Lae', 'account_type' => 'seller'],
            ['name' => 'MarketPG Test Admin', 'email' => env('MARKETPG_DEMO_ADMIN_EMAIL', 'admin.test@marketpg.test'), 'password' => env('MARKETPG_DEMO_ADMIN_PASSWORD', 'Admin-MPG-Test-2026!'), 'location' => 'Port Moresby', 'account_type' => 'both'],
            ['name' => 'MarketPG Test Buyer 2', 'email' => 'test@test.com', 'password' => env('MARKETPG_DEMO_EXTRA_BUYER_PASSWORD'), 'location' => 'Lae', 'account_type' => 'buyer'],
        ];

        foreach ($accounts as $account) {
            if (empty($account['email']) || empty($account['password'])) {
                throw new RuntimeException('Demo seeding requires all demo account emails and passwords.');
            }
            $user = User::updateOrCreate(
                ['email' => strtolower($account['email'])],
                ['name' => $account['name'], 'password' => Hash::make($account['password']), 'location' => $account['location'], 'account_type' => $account['account_type']]
            );
            if (!Hash::check($account['password'], $user->password)) {
                throw new RuntimeException('Demo account password verification failed for '.$account['email'].'.');
            }
        }

        $seller = User::where('email', 'seller.test@marketpg.test')->firstOrFail();
        $items = [
            [1, 'Samsung Galaxy S22', '128GB smartphone in good working condition. Includes charging cable.', 1450, 'used', 'Lae'],
            [2, 'iPhone 13 128GB', 'Unlocked iPhone 13, clean screen and healthy battery.', 2100, 'used', 'Port Moresby'],
            [1, 'HP EliteBook 840 G8', 'Business laptop, Core i5, 16GB RAM and 512GB SSD.', 2650, 'refurbished', 'Lae'],
            [1, 'Dell Latitude 5420', 'Reliable work laptop with charger and Windows installed.', 2200, 'used', 'Goroka'],
            [2, 'Apple iPad 9th Gen', '64GB Wi-Fi tablet, suitable for school and work.', 1250, 'used', 'Port Moresby'],
            [3, 'Toyota Corolla 2008', 'Automatic petrol sedan, roadworthy and ready for inspection.', 18500, 'used', 'Lae'],
            [3, 'Honda CR-V 2012', 'Family SUV, spacious interior and reliable engine.', 32500, 'used', 'Port Moresby'],
            [4, 'Three-Seater Sofa', 'Charcoal fabric sofa in good condition.', 950, 'used', 'Madang'],
            [4, 'Solid Timber Dining Set', 'Six-seat dining table with matching chairs.', 1800, 'used', 'Lae'],
            [5, 'Double Bed Frame', 'Strong timber double bed frame. Mattress not included.', 650, 'new', 'Goroka'],
            [5, 'Hisense 320L Refrigerator', 'Clean refrigerator with freezer compartment.', 1850, 'used', 'Port Moresby'],
            [1, 'Samsung 43-inch Smart TV', 'Full HD smart television with remote control.', 1350, 'used', 'Kokopo'],
            [6, 'Men’s Casual Sneakers', 'Comfortable everyday sneakers, size range 8–10.', 180, 'new', 'Lae'],
            [6, 'Ladies Handbag', 'Everyday handbag with multiple compartments.', 120, 'new', 'Port Moresby'],
            [7, 'Mountain Bicycle', '26-inch mountain bike with front suspension.', 550, 'used', 'Goroka'],
            [7, 'Football Boots', 'Firm-ground football boots, size 9.', 160, 'new', 'Lae'],
            [5, 'Microwave Oven 25L', 'Digital microwave, tested and working.', 420, 'used', 'Madang'],
            [8, 'Portable Generator 2.5kVA', 'Compact petrol generator for home backup.', 1650, 'used', 'Kokopo'],
            [1, 'TP-Link Wi-Fi Router', 'Dual-band AC1200 router, factory reset.', 190, 'new', 'Port Moresby'],
            [4, 'Office Desk and Chair', 'Compact office desk with ergonomic chair.', 780, 'used', 'Lae'],
        ];

        foreach ($items as [$category, $title, $description, $price, $condition, $location]) {
            Listing::updateOrCreate(
                ['user_id' => $seller->id, 'title' => $title],
                ['category_id' => $category, 'description' => $description, 'price' => $price, 'condition' => $condition, 'location' => $location, 'status' => 'active']
            );
        }
    }
}
