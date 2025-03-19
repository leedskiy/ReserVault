import { useState } from "react";
import { FaStar } from "react-icons/fa";

const HotelStars = ({ hotelData, setHotelData, direction = "vertical" }) => {
    const [hoveredStars, setHoveredStars] = useState(null);

    return (
        <div className={`flex flex-col w-1/8 h-auto ${direction === "vertical" ? "items-center" : ""}`}>
            <label className={`block text-gray-600  ${direction === "vertical" ? "mb-4" : ""}`}>Stars</label>
            <div
                className={`flex ${direction === "vertical" ? "flex-col flex-grow justify-between h-full" : "space-x-2"}`}
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
                                : index < hotelData.stars
                                    ? "#32492D"
                                    : "gray"
                        }
                        onMouseEnter={() => setHoveredStars(index + 1)}
                        onClick={() => setHotelData((prev) => ({ ...prev, stars: index + 1 }))}
                    />
                ))}
            </div>
        </div >
    );
};

export default HotelStars;