import { useParams, useNavigate } from "react-router-dom";
import Header from "../../components/common/Header";
import Sidebar from "../../components/common/Sidebar";
import { FaUser, FaLock, FaHotel } from "react-icons/fa";
import ProfileNameSection from "../../components/common/profile/ProfileNameSection";
import { useAuth } from "../../context/AuthContext";

const ProfilePage = () => {
    const { isManager } = useAuth();
    const navigate = useNavigate();
    const { view = "name" } = useParams();

    const sidebarOptions = [
        { label: "Name", value: "name", icon: FaUser },
        { label: "Security", value: "security", icon: FaLock },
        isManager && { label: "Hotels", value: "hotels", icon: FaHotel },
    ].filter(Boolean);

    return (
        <>
            <Header />
            <div className="container mx-auto max-w-6xl py-6">
                <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">Profile</h1>

                <div className="container max-w-6xl mx-auto flex gap-6">
                    <Sidebar
                        options={sidebarOptions}
                        activeView={view}
                        basePath="profile"
                    />

                    <div className="flex flex-col items-center w-full">
                        {view === "name" && <ProfileNameSection />}
                        {view === "hotels" && <ProfileNameSection />}
                        {view === "security" && <ProfileNameSection />}
                    </div>
                </div>
            </div>
        </>
    );
};

export default ProfilePage;