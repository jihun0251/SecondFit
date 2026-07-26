import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import { WishlistProvider } from "./contexts/WishlistContext";
import ProtectedRoute from "./components/ProtectedRoute";
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
    // AuthProvider가 가장 바깥 — 찜 목록은 로그인 여부를 알아야 불러올 수 있다
    <AuthProvider>
      <BrowserRouter>
        <WishlistProvider>
          <Routes>
            {/* 공개 */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/reset" element={<ResetPasswordPage />} />
            <Route path="/products" element={<ProductListPage />} />
            {/* /products/new 는 /products/:productId 보다 먼저 — "new"가 ID로 잡히지 않게 */}
            <Route path="/products/new" element={<ProtectedRoute><ProductCreatePage /></ProtectedRoute>} />
            <Route path="/products/:productId" element={<ProductDetailPage />} />

            {/* 로그인 필요 */}
            <Route path="/products/:productId/edit" element={<ProtectedRoute><ProductEditPage /></ProtectedRoute>} />
            <Route path="/my/likes" element={<ProtectedRoute><LikesPage /></ProtectedRoute>} />
            <Route path="/my/orders" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
            <Route path="/my/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
            <Route path="/my/reviews" element={<ProtectedRoute><ReviewsPage /></ProtectedRoute>} />
            <Route path="/my/sales" element={<ProtectedRoute><SalesPage /></ProtectedRoute>} />
            <Route path="/checkout/:productId" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
            <Route path="/checkout/:productId/address" element={<ProtectedRoute><AddressPage /></ProtectedRoute>} />
            <Route path="/orders/:orderId" element={<ProtectedRoute><DeliveryPage /></ProtectedRoute>} />

            {/* 관리자 전용 */}
            <Route path="/admin/inbounds" element={<ProtectedRoute adminOnly><AdminInboundsPage /></ProtectedRoute>} />
            <Route path="/admin/orders" element={<ProtectedRoute adminOnly><AdminOrdersPage /></ProtectedRoute>} />
            <Route path="/admin/settlements" element={<ProtectedRoute adminOnly><AdminSettlementsPage /></ProtectedRoute>} />
            <Route path="/admin/reports" element={<ProtectedRoute adminOnly><AdminReportsPage /></ProtectedRoute>} />
          </Routes>
        </WishlistProvider>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
