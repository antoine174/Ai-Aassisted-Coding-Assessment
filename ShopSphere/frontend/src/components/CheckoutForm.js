import React, { useState, useEffect } from 'react';
import { createOrder, getProducts } from '../api/orders';

const CUSTOMERS = [
  { id: 'user-1', name: 'Alice Vance', tier: 'STANDARD' },
  { id: 'user-2', name: 'Bob Sterling', tier: 'PRIME' },
];

const SHIPPING_METHODS = ['STANDARD', 'EXPRESS'];
const PROMO_CODES = ['', 'SAVE10', 'VIP20', 'FLAT15'];

function CheckoutForm() {
  const [products, setProducts] = useState([]);
  const [userId, setUserId] = useState('user-1');
  const [items, setItems] = useState([{ productId: '', quantity: 1 }]);
  const [shippingAddress, setShippingAddress] = useState('');
  const [shippingMethod, setShippingMethod] = useState('STANDARD');
  const [promoCode, setPromoCode] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getProducts()
      .then(setProducts)
      .catch(() => setProducts([]));
  }, []);

  const addItem = () => {
    setItems([...items, { productId: '', quantity: 1 }]);
  };

  const removeItem = (idx) => {
    setItems(items.filter((_, i) => i !== idx));
  };

  const updateItem = (idx, field, value) => {
    const updated = [...items];
    updated[idx] = { ...updated[idx], [field]: field === 'quantity' ? parseInt(value, 10) : value };
    setItems(updated);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setResult(null);
    setError(null);

    try {
      const order = await createOrder(userId, {
        items: items.filter(i => i.productId),
        shippingAddress,
        shippingMethod,
        promoCode: promoCode || undefined,
        notes: notes || undefined,
      });
      setResult(order);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const selectedCustomer = CUSTOMERS.find(c => c.id === userId);

  return (
    <div>
      <div className="page-header">
        <h1>Place an Order</h1>
        <p>Select products, enter your shipping details, and complete checkout</p>
      </div>

      <div className="grid-2">
        <div>
          <div className="card">
            <form id="checkout-form" onSubmit={handleSubmit}>
              {/* Customer Select */}
              <div className="form-group">
                <label>Customer</label>
                <select
                  id="customer-select"
                  className="form-control"
                  value={userId}
                  onChange={e => setUserId(e.target.value)}
                >
                  {CUSTOMERS.map(c => (
                    <option key={c.id} value={c.id}>
                      {c.name} ({c.tier})
                    </option>
                  ))}
                </select>
              </div>

              {/* Items */}
              <div className="form-group">
                <label>Order Items</label>
                <div className="items-container">
                  {items.map((item, idx) => (
                    <div key={idx} className="item-row">
                      <select
                        className="form-control"
                        value={item.productId}
                        onChange={e => updateItem(idx, 'productId', e.target.value)}
                        required
                      >
                        <option value="">Select product…</option>
                        {products.filter(p => p.active).map(p => (
                          <option key={p.id} value={p.id}>
                            {p.name} — ${p.price}
                          </option>
                        ))}
                      </select>
                      <input
                        type="number"
                        className="form-control"
                        min="1"
                        value={item.quantity}
                        onChange={e => updateItem(idx, 'quantity', e.target.value)}
                        required
                      />
                      {items.length > 1 && (
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          onClick={() => removeItem(idx)}
                        >
                          ✕
                        </button>
                      )}
                    </div>
                  ))}
                </div>
                <button type="button" className="btn btn-secondary btn-sm" onClick={addItem}>
                  + Add Item
                </button>
              </div>

              {/* Shipping Address */}
              <div className="form-group">
                <label>Shipping Address</label>
                <input
                  id="shipping-address"
                  type="text"
                  className="form-control"
                  placeholder="Enter delivery address"
                  value={shippingAddress}
                  onChange={e => setShippingAddress(e.target.value)}
                  required
                />
              </div>

              {/* Shipping Method */}
              <div className="form-group">
                <label>Shipping Method</label>
                <select
                  id="shipping-method"
                  className="form-control"
                  value={shippingMethod}
                  onChange={e => setShippingMethod(e.target.value)}
                >
                  {SHIPPING_METHODS.map(m => (
                    <option key={m} value={m}>{m}</option>
                  ))}
                </select>
              </div>

              <div className="grid-2">
                {/* Promo Code */}
                <div className="form-group">
                  <label>Promo Code</label>
                  <select
                    id="promo-code"
                    className="form-control"
                    value={promoCode}
                    onChange={e => setPromoCode(e.target.value)}
                  >
                    {PROMO_CODES.map(p => (
                      <option key={p} value={p}>{p || 'None'}</option>
                    ))}
                  </select>
                </div>

                {/* Notes */}
                <div className="form-group">
                  <label>Notes (optional)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Delivery notes…"
                    value={notes}
                    onChange={e => setNotes(e.target.value)}
                  />
                </div>
              </div>

              <button
                id="place-order-btn"
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
              >
                {loading ? <><div className="spinner" /> Placing Order…</> : '🛒 Place Order'}
              </button>
            </form>
          </div>
        </div>

        {/* Order Summary / Result */}
        <div>
          {selectedCustomer && (
            <div className="card" style={{ marginBottom: '1rem' }}>
              <h3 style={{ marginBottom: '0.75rem', fontSize: '0.95rem' }}>
                Customer Info
              </h3>
              <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                <div
                  style={{
                    width: 40,
                    height: 40,
                    borderRadius: '50%',
                    background: 'var(--accent-gradient)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 800,
                    fontSize: '1.1rem',
                  }}
                >
                  {selectedCustomer.name[0]}
                </div>
                <div>
                  <div style={{ fontWeight: 600 }}>{selectedCustomer.name}</div>
                  <span className={`badge badge-${selectedCustomer.tier.toLowerCase()}`}>
                    {selectedCustomer.tier}
                  </span>
                </div>
              </div>
            </div>
          )}

          {error && (
            <div className="alert alert-error fade-in">
              ⚠️ {error}
            </div>
          )}

          {result && (
            <div className="card fade-in">
              <div style={{ marginBottom: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3 style={{ color: '#22c55e' }}>✓ Order Placed!</h3>
                  <span className="badge badge-confirmed">CONFIRMED</span>
                </div>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: 4 }}>
                  {result.orderNumber}
                </div>
              </div>

              {result.items && result.items.map((item, idx) => (
                <div key={idx} className="price-row">
                  <span className="label">{item.productName} × {item.quantity}</span>
                  <span className="value">${item.lineTotal?.toFixed(2)}</span>
                </div>
              ))}

              <hr className="divider" />

              <div className="price-row">
                <span className="label">Subtotal</span>
                <span className="value">${result.subtotal?.toFixed(2)}</span>
              </div>
              {result.discountAmount > 0 && (
                <div className="price-row">
                  <span className="label">Discount ({result.promoCode})</span>
                  <span className="value discount">−${result.discountAmount?.toFixed(2)}</span>
                </div>
              )}
              <div className="price-row">
                <span className="label">Shipping ({result.shippingMethod})</span>
                <span className="value">${result.shippingFee?.toFixed(2)}</span>
              </div>
              <div className="price-row">
                <span className="label">Tax (8%)</span>
                <span className="value">${result.taxAmount?.toFixed(2)}</span>
              </div>
              <div className="price-row total">
                <span className="label">Total</span>
                <span className="value">${result.totalAmount?.toFixed(2)}</span>
              </div>

              {result.notes && (
                <div style={{ marginTop: '1rem', padding: '10px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                  📦 {result.notes}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default CheckoutForm;
