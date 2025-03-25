import {
    FaWifi, FaParking, FaSwimmingPool, FaSnowflake, FaCoffee
} from "react-icons/fa";

const FacilityIcons = ({ facilities = {}, size = 20 }) => {
    const iconProps = { size: size };

    const icons = [
        {
            key: "wifi",
            icon: FaWifi,
            label: "Wi-Fi",
            availableText: "Wi-Fi available",
            unavailableText: "Wi-Fi not available",
        },
        {
            key: "parking",
            icon: FaParking,
            label: "Parking",
            availableText: "Parking available",
            unavailableText: "Parking not available",
        },
        {
            key: "pool",
            icon: FaSwimmingPool,
            label: "Swimming pool",
            availableText: "Swimming pool available",
            unavailableText: "No swimming pool",
        },
        {
            key: "airConditioning",
            icon: FaSnowflake,
            label: "Air conditioning",
            availableText: "Air conditioning available",
            unavailableText: "No air conditioning",
        },
        {
            key: "breakfast",
            icon: FaCoffee,
            label: "Breakfast",
            availableText: "Breakfast included",
            unavailableText: "No breakfast included",
        },
    ];

    return (
        <div className="flex flex-wrap gap-2 items-center text-sm">
            {icons.map(({ key, icon: Icon, availableText, unavailableText }) => {
                const available = facilities[key];
                return (
                    <div
                        key={key}
                        className={`flex items-center space-x-1 ${available ? "text-[#32492D]" : "text-gray-400"}`}
                        title={available ? availableText : unavailableText}
                    >
                        <Icon {...iconProps} />
                    </div>
                );
            })}
        </div>
    );
};

export default FacilityIcons;