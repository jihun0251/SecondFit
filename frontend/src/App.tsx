import { BrowserRouter, Routes, Route } from "react-router-dom";
import { LikesProvider } from "./contexts/LikesContext";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import ProductListPage from "./pages/ProductListPage";
import ProductDetailPage from "./pages/ProductDetailPage";
import LikesPage from "./pages/LikesPage";
import CheckoutPage from "./pages/CheckoutPage";
import AddressPage from "./pages/AddressPage";
import OrdersPage from "./pages/OrdersPage";
import DeliveryPage from "./pages/DeliveryPage";
import ProfilePage from "./pages/ProfilePage";
import ReviewsPage from "./pages/ReviewsPage";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import ProductCreatePage from "./pages/ProductCreatePage";
import ProductEditPage from "./pages/ProductEditPage";
import SalesPage from "./pages/SalesPage";
import AdminInboundsPage from "./pages/admin/AdminInboundsPage";
import AdminOrdersPage from "./pages/admin/AdminOrdersPage";
import AdminSettlementsPage from "./pages/admin/AdminSettlementsPage";
import AdminReportsPage from "./pages/admin/AdminReportsPage";

function App() {
  return (
    <LikesProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/reset" element={<ResetPasswordPage />} />
          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/:productId" element={<ProductDetailPage />} />
          <Route path="/my/likes" element={<LikesPage />} />
          <Route path="/my/orders" element={<OrdersPage />} />
          <Route path="/my/profile" element={<ProfilePage />} />
          <Route path="/my/reviews" element={<ReviewsPage />} />
          <Route path="/checkout/:productId" element={<CheckoutPage />} />
          <Route path="/checkout/:productId/address" element={<AddressPage />} />
          <Route path="/orders/:orderId" element={<DeliveryPage />} />
          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/new" element={<ProductCreatePage />} />
          <Route path="/products/:productId" element={<ProductDetailPage />} />
          <Route path="/products/:productId/edit" element={<ProductEditPage />} />
          <Route path="/my/sales" element={<SalesPage />} />
          <Route path="/admin/inbounds" element={<AdminInboundsPage />} />
          <Route path="/admin/orders" element={<AdminOrdersPage />} />
          <Route path="/admin/settlements" element={<AdminSettlementsPage />} />
          <Route path="/admin/reports" element={<AdminReportsPage />} />
        </Routes>
      </BrowserRouter>
    </LikesProvider>
  );
}

export default App;