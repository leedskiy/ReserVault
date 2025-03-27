import React from "react";
import {
    FaWifi,
    FaParking,
    FaSwimmingPool,
    FaSnowflake,
    FaCoffee,
} from "react-icons/fa";

const facilityOptions = [
    { key: "wifi", icon: FaWifi, label: "Wi-Fi" },
    { key: "parking", icon: FaParking, label: "Parking" },
    { key: "pool", icon: FaSwimmingPool, label: "Pool" },
    { key: "airConditioning", icon: FaSnowflake, label: "Air Conditioning" },
    { key: "breakfast", icon: FaCoffee, label: "Breakfast" },
];

const FacilitiesSelector = ({ facilities, onToggle, layout = "horizontal", forOfferSearchSidebar = false }) => {
    const isVertical = layout === "vertical";

    return (
        <div className={`${isVertical ? "h-full flex flex-col items-center" : ""}`}>
            <div
                className={`flex ${forOfferSearchSidebar ? "justify-between" : isVertical ? "flex-col justify-between h-full" : "flex-wrap gap-4"}`}
            >
                {facilityOptions.map(({ key, icon: Icon, label }) => (
                    <button
                        key={key}
                        type="button"
                        title={label}
                        onClick={() => onToggle(key)}
                        className={`p-2 rounded-full transition-colors duration-200 ${facilities?.[key] ? "text-[#32492D]" : "text-gray-400"
                            }`}
                    >
                        <Icon size={24} />
                    </button>
                ))}
            </div>
        </div>
    );
};

export default FacilitiesSelector;