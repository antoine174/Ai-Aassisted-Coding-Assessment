const API_BASE = '/api';

export async function createOrder(userId, orderData) {
  const response = await fetch(`${API_BASE}/orders`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-user-id': userId,
    },
    body: JSON.stringify(orderData),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}`);
  }

  return response.json();
}

export async function searchOrders(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, value);
    }
  });

  const response = await fetch(`${API_BASE}/orders/search?${query.toString()}`);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}

export async function getProducts() {
  const response = await fetch(`${API_BASE}/products`);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}

export async function getCustomers() {
  const response = await fetch(`${API_BASE}/customers`);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}
