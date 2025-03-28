import { useSearchParams } from "react-router-dom";
import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Range } from "react-range";
import FacilitiesSelector from "../common/FacilitiesSelector";
import HotelStars from "../common/HotelStars";

const OfferSearchSidebar = ({ onApplyFilters }) => {
    // general
    const [searchParams] = useSearchParams();

    const [filters, setFilters] = useState(() => {
        const getBoolean = (key) => searchParams.get(key) === "true";
        const getNumber = (key, fallback = null) => {
            const val = searchParams.get(key);
            return val !== null ? Number(val) : fallback;
        };

        return {
            minPrice: getNumber("minPrice", 0),
            maxPrice: getNumber("maxPrice", 1000),
            wifi: getBoolean("wifi"),
            parking: getBoolean("parking"),
            pool: getBoolean("pool"),
            airConditioning: getBoolean("airConditioning"),
            breakfast: getBoolean("breakfast"),
            rating: searchParams.get("rating")
                ? [Number(searchParams.get("rating"))]
                : [],
            hotelStars: getNumber("hotelStars"),
        };
    });

    const applyFilters = () => {
        onApplyFilters({
            sortingOption,
            filters
        });
    };

    // sorting
    const [isSortingDropdownOpen, setIsSortingDropdownOpen] = useState(false);
    const sortingDropdownRef = useRef(null);

    const reverseSortingMap = {
        "price_asc": "Price (Low to High)",
        "price_desc": "Price (High to Low)",
        "rating_asc": "Offer Rating (Low to High)",
        "rating_desc": "Offer Rating (High to Low)",
        "stars_asc": "Hotel Stars (Low to High)",
        "stars_desc": "Hotel Stars (High to Low)",
    };

    const [sortingOption, setSortingOption] = useState(() => {
        const sortBy = searchParams.get("sortBy");
        const sortOrder = searchParams.get("sortOrder");
        if (sortBy && sortOrder) {
            const label = reverseSortingMap[`${sortBy}_${sortOrder}`];
            return label || null;
        }
        return null;
    });

    const handleSortingSelection = (option) => {
        setSortingOption(option);
        setIsSortingDropdownOpen(false);
    };

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (sortingDropdownRef.current && !sortingDropdownRef.current.contains(e.target)) {
                setIsSortingDropdownOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // facilities
    const handleFacilityToggle = (facilityKey) => {
        setFilters((prevFilters) => ({
            ...prevFilters,
            [facilityKey]: !prevFilters[facilityKey],
        }));
    };

    // rating
    const [rating, setRating] = useState(() => {
        const rating = searchParams.get("rating");
        return rating ? Number(rating) : null;
    });
    const [isRatingDropdownOpen, setIsRatingDropdownOpen] = useState(false);
    const ratingDropdownRef = useRef(null);

    const handleRatingSelection = (rating) => {
        setRating(rating);
        setIsRatingDropdownOpen(false);
        setFilters((prevFilters) => ({
            ...prevFilters,
            rating: rating ? [rating] : [],
        }));
    };

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (ratingDropdownRef.current && !ratingDropdownRef.current.contains(e.target)) {
                setIsRatingDropdownOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <motion.aside
            className="w-72 bg-white shadow-lg h-full p-4 rounded-md flex-shrink-0"
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, ease: "easeOut" }}
        >
            <motion.nav
                className="space-y-8"
                initial="hidden"
                animate="visible"
                variants={{
                    hidden: { opacity: 0 },
                    visible: { opacity: 1, transition: { staggerChildren: 0.15 } },
                }}
            >
                <div className="flex flex-col space-y-2">
                    <h3 className="text-lg font-semibold text-gray-900">Sort By</h3>
                    <div className="relative" ref={sortingDropdownRef}>
                        <button
                            onClick={() => setIsSortingDropdownOpen((prev) => !prev)}
                            className="w-full text-left py-2 px-4 border rounded-md bg-white 
                                focus:outline-none transition-all duration-100 ease-in-out transform 
                                rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D] text-gray-600"
                        >
                            {sortingOption ? sortingOption : "Select Sorting Option"}
                        </button>

                        <AnimatePresence>
                            {isSortingDropdownOpen && (
                                <motion.ul
                                    key="room-people-dropdown"
                                    className="absolute mt-2 w-full bg-white shadow-lg border border-gray-200 rounded-md z-10 text-gray-600"
                                    initial={{ opacity: 0, y: -10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10, transition: { duration: 0.2 } }}
                                >
                                    <li
                                        onClick={() => handleSortingSelection("Price (Low to High)")}
                                        className="px-4 py-2 cursor-pointer hover:bg-gray-200 rounded-t-md"
                                    >
                                        Price (Low to High)
                                    </li>
                                    <li
                                        onClick={() => handleSortingSelection("Price (High to Low)")}
                                        className="px-4 py-2 cursor-pointer hover:bg-gray-200"
                                    >
                                        Price (High to Low)
                                    </li>
                                    <li
                                        onClick={() => handleSortingSelection("Offer Rating (Low to High)")}
                                        className="px-4 py-2 cursor-pointer hover:bg-gray-200"
                                    >
                                        Offer Rating (Low to High)
                                    </li>
                                    <li
                                        onClick={() => handleSortingSelection("Offer Rating (High to Low)")}
                                        className="px-4 py-2 cursor-pointer hover:bg-gray-200"
                                    >
                                        Offer Rating (High to Low)
                                    </li>
                                    <li
                                        onClick={() => handleSortingSelection("Hotel Stars (Low to High)")}
                                        className="px-4 py-2 cursor-pointer hover:bg-gray-200"
                                    >
                                        Hotel Stars (Low to High)
                                    </li>
                                    <li
                                        onClick={() => handleSortingSelection("Hotel Stars (High to Low)")}
                                        className="px-4 py-2 cursor-pointer hover:bg-gray-200 rounded-b-md"
                                    >
                                        Hotel Stars (High to Low)
                                    </li>
                                </motion.ul>
                            )}
                        </AnimatePresence>
                    </div>
                </div>

                <div className="flex flex-col space-y-1">
                    <h3 className="text-lg font-semibold text-gray-900">Filter By</h3>

                    <div className="flex flex-col gap-y-4">
                        <div>
                            <div className="space-y-4">
                                <h4 className="text-base text-gray-600">
                                    Price Range: <span>{filters.minPrice}€</span> – <span>{filters.maxPrice}€</span>
                                </h4>
                                <div className="px-3">
                                    <Range
                                        values={[filters.minPrice, filters.maxPrice]}
                                        step={10}
                                        min={0}
                                        max={1000}
                                        onChange={(values) => {
                                            setFilters({
                                                ...filters,
                                                minPrice: values[0],
                                                maxPrice: values[1],
                                            });
                                        }}
                                        renderTrack={({ props, children }) => (
                                            <div
                                                {...props}
                                                style={{
                                                    ...props.style,
                                                    height: "6px",
                                                    background: "#ddd",
                                                    borderRadius: "4px",
                                                }}
                                            >
                                                {children}
                                            </div>
                                        )}
                                        renderThumb={({ props, index }) => {
                                            const { key, ...restProps } = props;

                                            return (
                                                <div
                                                    key={index}
                                                    {...restProps}
                                                    style={{
                                                        ...restProps.style,
                                                        height: "20px",
                                                        width: "20px",
                                                        borderRadius: "50%",
                                                        background: "#32492D",
                                                        boxShadow: "0 2px 10px rgba(0, 0, 0, 0.2)",
                                                    }}
                                                />
                                            );
                                        }}
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="mt-4">
                            <h4 className="text-base text-gray-600">Facilities</h4>
                            <FacilitiesSelector
                                facilities={filters}
                                onToggle={handleFacilityToggle}
                                forOfferSearchSidebar={true}
                            />
                        </div>

                        <div className="space-y-2" ref={ratingDropdownRef}>
                            <h4 className="text-base text-gray-600">Offer Rating</h4>
                            <div className="relative">
                                <button
                                    onClick={() => setIsRatingDropdownOpen((prev) => !prev)}
                                    className="w-full text-left py-2 px-4 border rounded-md bg-white focus:outline-none
                                        focus:outline-none transition-all duration-100 ease-in-out transform 
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D] text-gray-600"
                                >
                                    {rating ? `${rating}+` : "Select Offer Rating"}
                                </button>

                                <AnimatePresence>
                                    {isRatingDropdownOpen && (
                                        <motion.ul

                                            key="rating-dropdown"
                                            className="absolute mt-2 w-full bg-white shadow-lg border border-gray-200 rounded-md z-10 text-gray-600"
                                            initial={{ opacity: 0, y: -10 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            exit={{ opacity: 0, y: -10, transition: { duration: 0.2 } }}
                                        >
                                            <li
                                                onClick={() => handleRatingSelection(null)}
                                                className="px-4 py-2 cursor-pointer hover:bg-gray-200 rounded-t-md"
                                            >
                                                None
                                            </li>
                                            {[6, 7, 8, 9].map((rating, index) => {
                                                const isLast = index === 3;

                                                return (
                                                    <li
                                                        key={rating}
                                                        onClick={() => handleRatingSelection(rating)}
                                                        className={`px-4 py-2 cursor-pointer hover:bg-gray-200 ${isLast ? "rounded-b-md" : ""}`}
                                                    >
                                                        {rating}+
                                                    </li>
                                                );
                                            })}
                                        </motion.ul>
                                    )}
                                </AnimatePresence>
                            </div>
                        </div>

                        <div className="space-y-2 mt-2">
                            <h4 className="text-base text-gray-600">Hotel Stars</h4>
                            <HotelStars
                                hotelData={filters}
                                setHotelData={setFilters}
                                direction="horizontal"
                                forOfferSearchSidebar={true}
                            />
                        </div>
                    </div>
                </div>

                <div className="flex flex-col gap-2">
                    <button
                        onClick={applyFilters}
                        className="w-full py-2 px-4 bg-[#32492D] text-white rounded-md hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
                    >
                        Apply
                    </button>

                    <button
                        onClick={() => {
                            setFilters({
                                minPrice: 0,
                                maxPrice: 1000,
                                wifi: false,
                                parking: false,
                                pool: false,
                                airConditioning: false,
                                breakfast: false,
                                rating: [],
                                hotelStars: null,
                            });
                            setRating(null);
                            setSortingOption(null);

                            const preservedParams = new URLSearchParams();
                            const keysToPreserve = ["location", "rooms", "people", "dateFrom", "dateUntil", "selectedLocation"];
                            keysToPreserve.forEach((key) => {
                                const value = searchParams.get(key);
                                if (value !== null) {
                                    preservedParams.set(key, value);
                                }
                            });

                            onApplyFilters({
                                sortingOption: null,
                                filters: {
                                    minPrice: 0,
                                    maxPrice: 1000,
                                    wifi: false,
                                    parking: false,
                                    pool: false,
                                    airConditioning: false,
                                    breakfast: false,
                                    rating: [],
                                    hotelStars: null,
                                },
                            });

                            window.history.replaceState(null, "", `/offers/search?${preservedParams.toString()}`);
                        }}
                        className="text-gray-700 w-full px-4 py-2 border rounded-lg bg-white shadow-md hover:bg-gray-200 transition-all duration-300"
                    >
                        Clear
                    </button>
                </div>
            </motion.nav>
        </motion.aside>
    );
};

export default OfferSearchSidebar;
