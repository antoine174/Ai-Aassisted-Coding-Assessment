# ShopSphere (Java Spring Boot + React): Order Checkout & Order Search

## Overview
ShopSphere is an e-commerce platform where customers can browse products, place orders with promotional discounts, and track their order history. Currently, two backend issues are preventing customers from completing checkouts properly and filtering orders on the order management page. Your task is to fix these backend issues.

---

### Issue #1: Order creation fails to persist orders and miscalculates checkout pricing
Customers are unable to place new orders even when providing valid product items and quantities. The endpoint fails to save and return the created order properly.

### Expected API Behavior — Order Creation

#### POST /api/orders
**Purpose**
Creates a new order for the authenticated customer, deducts product stock, calculates pricing totals, and returns the created order.

**Request Headers**
```json
{
  "x-user-id": "string",     // Required: ID of the authenticated customer
  "Content-Type": "application/json"
}
```

**Request Body**
```json
{
  "items": [
    {
      "productId": "string", // Required: ID of the product
      "quantity": 2          // Required: Quantity to purchase (must be >= 1)
    }
  ],
  "shippingAddress": "string", // Required: Delivery address
  "shippingMethod": "string",  // Optional: "STANDARD" or "EXPRESS" (default: "STANDARD")
  "promoCode": "string",       // Optional: Promotional code (case-insensitive, e.g., "SAVE10" or "VIP20")
  "notes": "string"            // Optional: Delivery notes
}
```

**Success Response (201 Created)**
```json
{
  "_id": "string",
  "orderNumber": "string",
  "customer": {
    "id": "string",
    "name": "string",
    "email": "string",
    "tier": "string"
  },
  "items": [
    {
      "productId": "string",
      "productName": "string",
      "category": "string",
      "unitPrice": 100.0,
      "quantity": 2,
      "lineTotal": 200.0
    }
  ],
  "subtotal": 200.0,
  "discountAmount": 20.0,
  "shippingFee": 0.0,
  "taxAmount": 14.4,
  "totalAmount": 194.4,
  "promoCode": "SAVE10",
  "shippingMethod": "STANDARD",
  "shippingAddress": "string",
  "notes": "string",
  "status": "CONFIRMED",
  "createdAt": "ISO string",
  "updatedAt": "ISO string"
}
```

**Order Creation Business Rules**:
1. **Validation & Stock Deduction**:
   - If `x-user-id` is missing/blank or the user does not exist, return `400 Bad Request` (or `404 Not Found` if user not found).
   - If `items` is null/empty, `shippingAddress` is missing/blank, any product does not exist, any product has `active = false`, or `product.stock < item.quantity`, return `400 Bad Request`.
   - Deduct the purchased `quantity` from each product's `stock` and persist the updated product in the database.
2. **Pricing Calculation (Round all monetary fields to 2 decimal places using standard half-up rounding)**:
   - `lineTotal` = `unitPrice * quantity` for each item.
   - `subtotal` = sum of all item `lineTotal` values.
   - `discountAmount`:
     - Promo code `"SAVE10"` (case-insensitive): **10%** off `subtotal`.
     - Promo code `"VIP20"` (case-insensitive): **20%** off `subtotal`, **only valid if `subtotal >= 100.0`** (otherwise `0.0` discount).
     - Promo code `"FLAT15"` (case-insensitive): **$15.00** flat off `subtotal`, **only valid if `subtotal >= 50.0`** (otherwise `0.0` discount).
     - If no valid promo code is applied, `discountAmount` is `0.0`. Store normalized uppercase `promoCode` if provided, or `null` if omitted/blank.
   - `shippingFee`:
     - Defaults to `"STANDARD"` if `shippingMethod` is null or blank (normalize to uppercase).
     - `"EXPRESS"`: always **$15.00**.
     - `"STANDARD"`: **$0.00 (free)** if `(subtotal - discountAmount) >= 100.0` OR if the customer's `tier` is `"PRIME"` (case-insensitive); otherwise **$7.99**.
   - `taxAmount` = **8%** (`0.08`) of `(subtotal - discountAmount)`.
   - `totalAmount` = `(subtotal - discountAmount) + shippingFee + taxAmount`.
3. **Defaults & Persistence**:
   - `status` must always be set to `"CONFIRMED"` when a new order is created.
   - If `notes` is not provided or blank, auto-generate it as: `"Standard delivery to " + shippingAddress` (if `shippingMethod` is `"STANDARD"`) or `"Express delivery to " + shippingAddress` (if `shippingMethod` is `"EXPRESS"`).
   - The order must be saved to the database with `createdAt` and `updatedAt` timestamps and a generated `orderNumber` (e.g., `"ORD-" + id` or UUID-based).

---

### Issue #2: Order search and filtering is not working as intended
The order search endpoint ignores filter parameters, fails on partial/case-insensitive text queries, and returns improperly sorted orders.

### Expected API Behavior — Order Search

#### GET /api/orders/search
**Purpose**
Searches and filters orders across text queries, status lists, total amount ranges, shipping methods, and categories.

**Auth**: Not required

**Query Parameters (All Optional)**:
- `q`: Search query string (case-insensitive, partial match)
- `type`: Search scope — `"order"`, `"customer"`, `"product"`, or `"all"` (default: `"all"`)
  - `"order"`: Matches `q` against `orderNumber` or `shippingAddress`
  - `"customer"`: Matches `q` against `customer.name` or `customer.email`
  - `"product"`: Matches `q` against any item's `productName` in the order
  - `"all"`: Matches `q` against any of the above fields (`orderNumber`, `shippingAddress`, `customer.name`, `customer.email`, or any item's `productName`)
- `status`: Single status string or comma-separated statuses (case-insensitive, e.g., `"CONFIRMED"` or `"CONFIRMED,SHIPPED"`). Returns orders matching **at least one** specified status.
- `minTotal`: Minimum `totalAmount` (inclusive, supports decimals, e.g., `50.5`)
- `maxTotal`: Maximum `totalAmount` (inclusive, supports decimals, e.g., `500.0`)
- `shippingMethod`: Exact match (case-insensitive) on `"STANDARD"` or `"EXPRESS"`
- `category`: Single category or comma-separated categories (case-insensitive, e.g., `"Electronics,Books"`). Returns orders where **at least one** purchased item belongs to one of the specified categories.

**Additional Rules for Order Search**:
- Orders with `status` equal to `"CANCELLED"` or `"ARCHIVED"` must **never** be returned unless `"CANCELLED"` or `"ARCHIVED"` is **explicitly** requested in the `status` query parameter. When no `status` filter is provided, exclude `"CANCELLED"` and `"ARCHIVED"` orders.
- Display orders that match **all** applied filter categories (AND logic across parameters; OR logic within comma-separated `status` and `category` values).
- Sort results by `totalAmount` in **descending** order, with `createdAt` in **descending** order (newest first) as a secondary sort when `totalAmount` values are equal.
