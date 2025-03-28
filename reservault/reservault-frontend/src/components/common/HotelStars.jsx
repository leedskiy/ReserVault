import { useState } from "react";
import { FaStar } from "react-icons/fa";

const HotelStars = ({ hotelData, setHotelData, direction = "vertical", forOfferSearchSidebar = false }) => {
    const [hoveredStars, setHoveredStars] = useState(null);
    const starField = forOfferSearchSidebar ? "hotelStars" : "stars";
    const selectedStars = hotelData?.[starField] ?? 0;

    const handleClick = (index) => {
        setHotelData((prev) => ({
            ...prev,
            [starField]: index + 1,
        }));
    };

    return (
        <div className={`flex flex-col w-1/8 h-full ${direction === "vertical" ? "items-center" : ""}`}>
            <div
                className={`flex ${direction === "vertical" ? "flex-col flex-grow justify-between h-full" : "space-x-2"} ${forOfferSearchSidebar ? "justify-between" : ""}`}
                onMouseLeave={() => setHoveredStars(null)}
            >
                {[...Array(5)].map((_, index) => (
                    <FaStar
                        key={index}
                        size={40}
                        className="cursor-pointer transition-colors duration-300"
                        color={
                            hoveredStars !== null
                                ? index < hoveredStars
                                    ? "#32492D"
                                    : "gray"
                                : index < selectedStars
                                    ? "#32492D"
                                    : "gray"
                        }
                        onMouseEnter={() => setHoveredStars(index + 1)}
                        onClick={() => handleClick(index)}
                    />
                ))}
            </div>
        </div >
    );
};

export default HotelStars;