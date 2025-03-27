import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Range } from "react-range";
import FacilitiesSelector from "../common/FacilitiesSelector";
import HotelStars from "../common/HotelStars";

const OfferSearchSidebar = ({ onApplyFilters }) => {
    // general
    const [filters, setFilters] = useState({
        minPrice: 0,
        maxPrice: 1000,
        wifi: false,
        parking: false,
        pool: false,
        airConditioning: false,
        breakfast: false,
        offerScore: [6, 7, 8, 9],
        hotelStars: [1, 2, 3, 4, 5],
    });

    const applyFilters = () => {
        onApplyFilters({
            sortingOption,
            filters
        });
    };

    // sorting
    const [sortingOption, setSortingOption] = useState(null);
    const [isSortingDropdownOpen, setIsSortingDropdownOpen] = useState(false);
    const sortingDropdownRef = useRef(null);

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

    // score
    const [offerScore, setOfferScore] = useState(null);
    const [isOfferScoreDropdownOpen, setIsOfferScoreDropdownOpen] = useState(false);
    const offerScoreDropdownRef = useRef(null);

    const handleOfferScoreSelection = (score) => {
        setOfferScore(score);
        setIsOfferScoreDropdownOpen(false);
        setFilters((prevFilters) => ({
            ...prevFilters,
            offerScore: score ? [score] : [],
        }));
    };

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (offerScoreDropdownRef.current && !offerScoreDropdownRef.current.contains(e.target)) {
                setIsOfferScoreDropdownOpen(false);
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
                                rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                        >
                            {sortingOption ? sortingOption : "Select Sorting Option"}
                        </button>

                        <AnimatePresence>
                            {isSortingDropdownOpen && (
                                <motion.ul
                                    key="room-people-dropdown"
                                    className="absolute mt-2 w-full bg-white shadow-lg border border-gray-200 rounded-md z-10"
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

                        <div className="space-y-2" ref={offerScoreDropdownRef}>
                            <h4 className="text-base text-gray-600">Offer Score</h4>
                            <div className="relative">
                                <button
                                    onClick={() => setIsOfferScoreDropdownOpen((prev) => !prev)}
                                    className="w-full text-left py-2 px-4 border rounded-md bg-white focus:outline-none
                                        focus:outline-none transition-all duration-100 ease-in-out transform 
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                >
                                    {offerScore ? `${offerScore}+` : "Select Offer Score"}
                                </button>

                                <AnimatePresence>
                                    {isOfferScoreDropdownOpen && (
                                        <motion.ul

                                            key="offer-score-dropdown"
                                            className="absolute mt-2 w-full bg-white shadow-lg border border-gray-200 rounded-md z-10"
                                            initial={{ opacity: 0, y: -10 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            exit={{ opacity: 0, y: -10, transition: { duration: 0.2 } }}
                                        >
                                            <li
                                                onClick={() => handleOfferScoreSelection(null)}
                                                className="px-4 py-2 cursor-pointer hover:bg-gray-200 rounded-t-md"
                                            >
                                                None
                                            </li>
                                            {[6, 7, 8, 9].map((score, index) => {
                                                const isLast = index === 3;

                                                return (
                                                    <li
                                                        key={score}
                                                        onClick={() => handleOfferScoreSelection(score)}
                                                        className={`px-4 py-2 cursor-pointer hover:bg-gray-200 ${isLast ? "rounded-b-md" : ""}`}
                                                    >
                                                        {score}+
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

                <div>
                    <button
                        onClick={applyFilters}
                        className="w-full py-2 px-4 bg-[#32492D] text-white rounded-md bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
                    >
                        Apply
                    </button>
                </div>
            </motion.nav>
        </motion.aside>
    );
};

export default OfferSearchSidebar;
