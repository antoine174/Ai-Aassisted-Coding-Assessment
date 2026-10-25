import React, { useState, useEffect, useCallback } from 'react';
import { searchOrders } from '../api/orders';

const STATUS_OPTIONS = ['CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'ARCHIVED'];
const CATEGORY_OPTIONS = ['Electronics', 'Furniture', 'Books', 'Accessories'];
const TYPE_OPTIONS = [
  { value: 'all', label: 'All Fields' },
  { value: 'order', label: 'Order' },
  { value: 'customer', label: 'Customer' },
  { value: 'product', label: 'Product' },
];

function statusBadgeClass(status) {
  const map = {
    CONFIRMED: 'confirmed',
    SHIPPED: 'shipped',
    DELIVERED: 'delivered',
    CANCELLED: 'cancelled',
    ARCHIVED: 'archived',
  };
  return `badge badge-${map[status] || 'standard'}`;
}

function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [q, setQ] = useState('');
  const [type, setType] = useState('all');
  const [selectedStatuses, setSelectedStatuses] = useState([]);
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [minTotal, setMinTotal] = useState('');
  const [maxTotal, setMaxTotal] = useState('');
  const [shippingMethod, setShippingMethod] = useState('');

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        q: q || undefined,
        type: type !== 'all' ? type : undefined,
        status: selectedStatuses.length > 0 ? selectedStatuses.join(',') : undefined,
        category: selectedCategories.length > 0 ? selectedCategories.join(',') : undefined,
        minTotal: minTotal || undefined,
        maxTotal: maxTotal || undefined,
        shippingMethod: shippingMethod || undefined,
      };
      const data = await searchOrders(params);
      setOrders(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [q, type, selectedStatuses, selectedCategories, minTotal, maxTotal, shippingMethod]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const toggleStatus = (s) => {
    setSelectedStatuses(prev =>
      prev.includes(s) ? prev.filter(x => x !== s) : [...prev, s]
    );
  };

  const toggleCategory = (c) => {
    setSelectedCategories(prev =>
      prev.includes(c) ? prev.filter(x => x !== c) : [...prev, c]
    );
  };

  return (
    <div>
      <div className="page-header">
        <h1>Order History</h1>
        <p>Search and filter all orders across the platform</p>
      </div>

      {/* Filter Bar */}
      <div className="filter-bar">
        <div className="filter-row" style={{ marginBottom: '12px' }}>
          <div className="filter-group" style={{ flex: 2 }}>
            <label>Search</label>
            <input
              id="search-input"
              type="text"
              className="form-control"
              placeholder="Search orders, customers, products…"
              value={q}
              onChange={e => setQ(e.target.value)}
            />
          </div>
          <div className="filter-group">
            <label>Search Type</label>
            <select
              id="search-type"
              className="form-control"
              value={type}
              onChange={e => setType(e.target.value)}
            >
              {TYPE_OPTIONS.map(o => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>
          <div className="filter-group">
            <label>Shipping</label>
            <select
              id="shipping-filter"
              className="form-control"
              value={shippingMethod}
              onChange={e => setShippingMethod(e.target.value)}
            >
              <option value="">Any</option>
              <option value="STANDARD">STANDARD</option>
              <option value="EXPRESS">EXPRESS</option>
            </select>
          </div>
          <div className="filter-group" style={{ flex: '0 0 auto' }}>
            <label>&nbsp;</label>
            <button id="search-btn" className="btn btn-primary" onClick={fetchOrders}>
              🔍 Search
            </button>
          </div>
        </div>

        <div className="filter-row">
          <div className="filter-group">
            <label>Min Total ($)</label>
            <input
              id="min-total"
              type="number"
              className="form-control"
              placeholder="0"
              value={minTotal}
              onChange={e => setMinTotal(e.target.value)}
            />
          </div>
          <div className="filter-group">
            <label>Max Total ($)</label>
            <input
              id="max-total"
              type="number"
              className="form-control"
              placeholder="999"
              value={maxTotal}
              onChange={e => setMaxTotal(e.target.value)}
            />
          </div>

          <div className="filter-group" style={{ flex: 2 }}>
            <label>Status</label>
            <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', paddingTop: 4 }}>
              {STATUS_OPTIONS.map(s => (
                <button
                  key={s}
                  type="button"
                  className={`btn btn-sm ${selectedStatuses.includes(s) ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => toggleStatus(s)}
                >
                  {s}
                </button>
              ))}
            </div>
          </div>

          <div className="filter-group" style={{ flex: 2 }}>
            <label>Category</label>
            <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', paddingTop: 4 }}>
              {CATEGORY_OPTIONS.map(c => (
                <button
                  key={c}
                  type="button"
                  className={`btn btn-sm ${selectedCategories.includes(c) ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => toggleCategory(c)}
                >
                  {c}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Results */}
      {loading && (
        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
          <div className="spinner" style={{ margin: '0 auto 12px' }} />
          Loading orders…
        </div>
      )}

      {error && <div className="alert alert-error">⚠️ {error}</div>}

      {!loading && !error && (
        <>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1rem' }}>
            {orders.length} order{orders.length !== 1 ? 's' : ''} found
          </div>

          {orders.length === 0 ? (
            <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
              📭 No orders match your filters
            </div>
          ) : (
            orders.map(order => (
              <div key={order._id} className="order-card fade-in">
                <div className="order-card-header">
                  <div>
                    <div className="order-number">{order.orderNumber}</div>
                    <div className="order-meta">
                      <span className={statusBadgeClass(order.status)}>{order.status}</span>
                      <span className="badge badge-standard">{order.shippingMethod}</span>
                      {order.customer?.tier === 'PRIME' && (
                        <span className="badge badge-prime">PRIME</span>
                      )}
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div className="order-total-amount">${order.totalAmount?.toFixed(2)}</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 2 }}>
                      {order.customer?.name}
                    </div>
                  </div>
                </div>

                <div className="order-items-list">
                  {order.items?.map((item, idx) => (
                    <div key={idx} className="order-item-row">
                      <span>
                        <span className="item-name">{item.productName}</span>
                        <span style={{ marginLeft: 6, fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                          ({item.category})
                        </span>
                      </span>
                      <span>{item.quantity} × ${item.unitPrice?.toFixed(2)} = ${item.lineTotal?.toFixed(2)}</span>
                    </div>
                  ))}
                </div>

                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  📍 {order.shippingAddress}
                  {order.promoCode && (
                    <span style={{ marginLeft: 12, color: 'var(--success)' }}>🏷 {order.promoCode}</span>
                  )}
                </div>
              </div>
            ))
          )}
        </>
      )}
    </div>
  );
}

export default OrderHistory;
