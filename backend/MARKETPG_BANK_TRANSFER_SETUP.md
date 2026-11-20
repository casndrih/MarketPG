# MarketPG platform bank-transfer setup

The application includes a safe PNG bank-transfer instruction template in `config/marketpg.php`. It is intentionally marked as a setup template and checkout remains blocked while it is present.

## Configure the real platform account

In Render, open the MarketPG web service environment and set:

- Key: `MARKETPG_BANK_TRANSFER_DETAILS`
- Value (replace every bracketed value):

```text
MARKETPG PLATFORM BANK TRANSFER
Bank: [YOUR PNG BANK]
Account name: [MARKETPG REGISTERED LEGAL ACCOUNT NAME]
Account number: [PLATFORM BANK ACCOUNT NUMBER]
Branch: [BRANCH / LOCATION]
Reference: Use the MarketPG order number as the transfer reference.
Payment note: Upload your bank receipt in MarketPG. Orders are not marked paid until an administrator verifies the deposit against the platform bank statement.
```

Use the registered MarketPG platform account, not a seller's personal account. Never put a real account number in source control. Save the Render environment variable and redeploy/restart the service so Laravel reloads its cached configuration.

## Important safeguards

- Do not remove the placeholders until the account details have been verified.
- Checkout rejects the default template, so buyers cannot be instructed to pay a fake account.
- Receipt approval must be based on the actual bank statement.
- Seller payouts are separate manual transfers after the buyer confirms delivery.
- This is a manual bank-transfer workflow, not an automated escrow or payment gateway.
