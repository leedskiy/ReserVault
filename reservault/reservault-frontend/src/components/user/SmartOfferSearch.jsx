import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate, useLocation } from "react-router-dom";

const SmartOfferSearch = () => {
    const navigate = useNavigate();
    const locationObj = useLocation();
    const params = new URLSearchParams(locationObj.search);

    const [location, setLocation] = useState(params.get("location") || "");
    const [rooms, setRooms] = useState(Number(params.get("rooms")) || 1);
    const [people, setPeople] = useState(Number(params.get("people")) || 1);
    const [dateFrom, setDateFrom] = useState(params.get("dateFrom") || "");
    const [dateUntil, setDateUntil] = useState(params.get("dateUntil") || "");

    const [showDropdown, setShowDropdown] = useState(false);
    const [error, setError] = useState("");
    const dropdownRef = useRef(null);

    const handleSearch = () => {
        const isValid = location.trim() && dateFrom && dateUntil;
        setError(isValid ? "" : "error");

        if (!isValid) return;

        const newParams = new URLSearchParams();
        newParams.append("location", location);
        newParams.append("rooms", rooms);
        newParams.append("people", people);
        newParams.append("dateFrom", dateFrom);
        newParams.append("dateUntil", dateUntil);

        navigate(`/offers/search?${newParams.toString()}`);
    };


    useEffect(() => {
        const handleClickOutside = (e) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const errorInputClass = "border-red-500 ring-red-500";

    return (
        <div className="flex flex-col items-center justify-center w-full">
            <div className="rounded-lg w-full max-w-4xl flex flex-col space-y-4">
                <div className="flex">
                    <div className="grid grid-cols-2 gap-6 flex-grow relative">
                        <div>
                            <input
                                type="text"
                                value={location}
                                onChange={(e) => setLocation(e.target.value)}
                                placeholder="Where are you going?"
                                className={`bg-white shadow-lg w-full px-4 py-2 border rounded-lg focus:outline-none transition-all duration-100 ease-in-out transform
                                    focus:ring-1 ${!location && error ? errorInputClass : "focus:border-[#32492D] focus:ring-[#32492D]"
                                    }`}
                            />
                        </div>

                        <div className="relative" ref={dropdownRef}>
                            <button
                                onClick={() => setShowDropdown((prev) => !prev)}
                                className="text-left bg-white shadow-lg w-full px-4 py-2 border border-gray-300 transition-all duration-100 ease-in-out transform
                                rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                            >
                                {rooms} Room{rooms > 1 ? "s" : ""}, {people} {people > 1 ? "People" : "Person"}
                            </button>

                            <AnimatePresence>
                                {showDropdown && (
                                    <motion.div
                                        key="room-people-dropdown"
                                        className="absolute mt-2 w-full bg-white shadow-lg border border-gray-200 rounded-md z-10 p-4"
                                        initial={{ opacity: 0, y: -10 }}
                                        animate={{ opacity: 1, y: 0 }}
                                        exit={{ opacity: 0, y: -10, transition: { duration: 0.2 } }}
                                    >
                                        <div className="flex justify-between items-center mb-4">
                                            <span>Rooms</span>
                                            <input
                                                type="number"
                                                min={1}
                                                value={rooms}
                                                onChange={(e) => setRooms(Number(e.target.value))}
                                                className="w-16 border rounded-md px-2 py-1 text-right"
                                            />
                                        </div>
                                        <div className="flex justify-between items-center">
                                            <span>People</span>
                                            <input
                                                type="number"
                                                min={1}
                                                value={people}
                                                onChange={(e) => setPeople(Number(e.target.value))}
                                                className="w-16 border rounded-md px-2 py-1 text-right"
                                            />
                                        </div>
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>

                        <div>
                            <input
                                type="date"
                                value={dateFrom}
                                onChange={(e) => setDateFrom(e.target.value)}
                                className={`bg-white shadow-lg w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-1 transition-all duration-100 ease-in-out transform
                                    ${!dateFrom && error ? errorInputClass : "focus:border-[#32492D] focus:ring-[#32492D]"
                                    }`}
                            />
                        </div>

                        <div>
                            <input
                                type="date"
                                value={dateUntil}
                                onChange={(e) => setDateUntil(e.target.value)}
                                className={`bg-white shadow-lg w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-1 transition-all duration-100 ease-in-out transform
                                    ${!dateUntil && error ? errorInputClass : "focus:border-[#32492D] focus:ring-[#32492D]"
                                    }`}
                            />
                        </div>
                    </div>

                    <div className="flex flex-col justify-center pl-6">
                        <button
                            onClick={handleSearch}
                            className="shadow-lg bg-[#32492D] hover:bg-[#273823] text-white font-semibold px-6 py-2 rounded-md transition-all duration-200 ease-in-out transform"
                        >
                            Search
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SmartOfferSearch;
