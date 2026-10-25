import React, { useState } from 'react';
import CheckoutForm from './components/CheckoutForm';
import OrderHistory from './components/OrderHistory';

function App() {
  const [activeTab, setActiveTab] = useState('checkout');

  return (
    <div className="app">
      <nav className="navbar">
        <div className="navbar-inner">
          <span className="navbar-brand">
            <span className="logo-icon">🛍️</span>
            ShopSphere
          </span>
          <div className="navbar-tabs">
            <button
              className={`navbar-tab ${activeTab === 'checkout' ? 'active' : ''}`}
              onClick={() => setActiveTab('checkout')}
            >
              🛒 Checkout
            </button>
            <button
              className={`navbar-tab ${activeTab === 'orders' ? 'active' : ''}`}
              onClick={() => setActiveTab('orders')}
            >
              📦 Order History
            </button>
          </div>
        </div>
      </nav>

      <main className="main-content">
        {activeTab === 'checkout' && <CheckoutForm />}
        {activeTab === 'orders' && <OrderHistory />}
      </main>
    </div>
  );
}

export default App;
