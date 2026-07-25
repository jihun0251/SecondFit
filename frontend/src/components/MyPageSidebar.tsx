import { Link, useLocation } from "react-router-dom";
import "./MyPageSidebar.css";

const menu = [
  { group: "내 계정", items: [{ label: "프로필 관리", path: "/my/profile" }] },
  {
    group: "거래",
    items: [
      { label: "판매 내역", path: "/my/sales" },
      { label: "구매 내역", path: "/my/orders" },
      { label: "찜 목록", path: "/my/likes" },
      { label: "받은 리뷰", path: "/my/reviews" },
    ],
  },
];

function MyPageSidebar() {
  const location = useLocation();

  return (
    <aside className="mypage-sidebar">
      {menu.map((section) => (
        <div className="sidebar-section" key={section.group}>
          <span className="sidebar-group">{section.group}</span>
          {section.items.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`sidebar-item ${
                location.pathname === item.path ? "active" : ""
              }`}
            >
              {item.label}
            </Link>
          ))}
        </div>
      ))}
    </aside>
  );
}

export default MyPageSidebar;