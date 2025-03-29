import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate, useLocation } from "react-router-dom";
import DateRangeSelector from "../common/DateRangeSelector";

const SmartOfferSearch = () => {
    const navigate = useNavigate();
    const locationObj = useLocation();
    const params = new URLSearchParams(locationObj.search);

    const [location, setLocation] = useState(params.get("location") || "");
    const [rooms, setRooms] = useState(Number(params.get("rooms")) || 1);
    const [people, setPeople] = useState(Number(params.get("people")) || 1);
    const [dateFrom, setDateFrom] = useState(params.get("dateFrom") || "");
    const [dateUntil, setDateUntil] = useState(params.get("dateUntil") || "");
    const [selectedLocation, setSelectedLocation] = useState(params.get("selectedLocation") || null);

    const [showRoomDropdown, setShowRoomDropdown] = useState(false);
    const [showCalendar, setShowCalendar] = useState(false);
    const [error, setError] = useState("");

    const roomRef = useRef(null);
    const calendarRef = useRef(null);

    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestions, setShowSuggestions] = useState(false);

    useEffect(() => {
        const fetchSuggestions = async () => {
            if (location.length < 2 || selectedLocation) {
                setSuggestions([]);
                setShowSuggestions(false);
                return;
            }

            try {
                const geoNamesUsername = import.meta.env.VITE_GEONAMES_USERNAME;
                const cityRes = await fetch(
                    `https://secure.geonames.org/searchJSON?q=${location}&maxRows=5&featureClass=P&username=${geoNamesUsername}`
                );
                const cityData = await cityRes.json();

                const citySuggestions = cityData.geonames?.map((place) => ({
                    id: place.geonameId,
                    display: `${place.name}, ${place.countryName}`,
                })) || [];

                let countrySuggestions = [];
                if (location.length >= 2) {
                    const countryRes = await fetch(
                        `https://secure.geonames.org/countryInfoJSON?username=${geoNamesUsername}`
                    );
                    const countryData = await countryRes.json();

                    countrySuggestions = countryData.geonames
                        .filter((country) =>
                            country.countryName.toLowerCase().includes(location.toLowerCase())
                        )
                        .slice(0, 3)
                        .map((country) => ({
                            id: `country-${country.geonameId}`,
                            display: country.countryName,
                        }));
                }

                const combined = [...citySuggestions, ...countrySuggestions];
                setSuggestions(combined);
                setShowSuggestions(true);
            } catch (err) {
                console.error("GeoNames error:", err);
            }
        };

        const timeout = setTimeout(fetchSuggestions, 300);
        return () => clearTimeout(timeout);
    }, [location, selectedLocation]);


    const handleSearch = () => {
        const isValid = selectedLocation && dateFrom && dateUntil;
        setError(isValid ? "" : "error");

        if (!isValid) return;

        const newParams = new URLSearchParams();
        newParams.append("location", location);
        newParams.append("rooms", rooms);
        newParams.append("people", people);
        newParams.append("dateFrom", dateFrom);
        newParams.append("dateUntil", dateUntil);
        newParams.append("selectedLocation", selectedLocation);

        navigate(`/offers/search?${newParams.toString()}`);
    };

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (roomRef.current && !roomRef.current.contains(e.target)) {
                setShowRoomDropdown(false);
            }
            if (calendarRef.current && !calendarRef.current.contains(e.target)) {
                setShowCalendar(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const errorInputClass = "border-red-500 ring-red-500";

    return (
        <div className="flex flex-col items-center justify-center w-full">
            <div className="rounded-lg w-full flex flex-col space-y-4">
                <div className="flex">
                    <div className="flex gap-2 flex-grow ">
                        <div className="w-2/5 relative">
                            <input
                                type="text"
                                value={location}
                                onChange={(e) => {
                                    setLocation(e.target.value);
                                    setSelectedLocation(null);
                                }}
                                placeholder="Where are you going?"
                                className={`text-gray-600 shadow-lg w-full px-4 py-2 border rounded-lg focus:outline-none transition-all duration-100 ease-in-out transform
                                        focus:ring-1 ${!selectedLocation && error
                                        ? errorInputClass
                                        : "focus:border-[#32492D] focus:ring-[#32492D]"}`}
                            />

                            <AnimatePresence>
                                {showSuggestions && !selectedLocation && (
                                    (() => {
                                        const filteredSuggestions = suggestions
                                            .map((place, index) => {
                                                const display = place.display;
                                                if (!display) return null;

                                                const isFirst = index === 0;
                                                const isLast = index === suggestions.length - 1;

                                                return (
                                                    <li
                                                        key={place.id || `${display}-${index}`}
                                                        onClick={() => {
                                                            setLocation(display);
                                                            setSelectedLocation(place);
                                                            setShowSuggestions(false);
                                                            setSuggestions([]);
                                                        }}
                                                        className={`text-gray-600 px-4 py-2 cursor-pointer hover:bg-gray-200 ${isFirst ? "rounded-t-md" : ""
                                                            } ${isLast ? "rounded-b-md" : ""}`}
                                                    >
                                                        {display}
                                                    </li>
                                                );
                                            })
                                            .filter(Boolean);

                                        return filteredSuggestions.length > 0 ? (
                                            <motion.ul
                                                key="suggestion-dropdown"
                                                className="absolute top-full left-0 bg-white border rounded-md shadow-lg w-full z-10 mt-2 overflow-hidden"
                                                initial={{ opacity: 0, y: -10 }}
                                                animate={{ opacity: 1, y: 0 }}
                                                exit={{ opacity: 0, y: -10 }}
                                            >
                                                {filteredSuggestions}
                                            </motion.ul>
                                        ) : null;
                                    })()
                                )}
                            </AnimatePresence>
                        </div>

                        <div className="w-2/5 relative" ref={calendarRef}>
                            <DateRangeSelector
                                startDate={dateFrom}
                                endDate={dateUntil}
                                onChange={({ startDate, endDate }) => {
                                    setDateFrom(startDate);
                                    setDateUntil(endDate);
                                }}
                                hasError={!dateFrom && !dateUntil && error}
                            />
                        </div>

                        <div className="relative w-1/5" ref={roomRef}>
                            <button
                                onClick={() => setShowRoomDropdown((prev) => !prev)}
                                className="text-gray-600 text-left bg-white shadow-lg w-full px-4 py-2 border rounded-lg focus:outline-none transition-all duration-100 ease-in-out transform
                                rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                            >
                                {rooms} Room{rooms > 1 ? "s" : ""}, {people} {people > 1 ? "People" : "Person"}
                            </button>

                            <AnimatePresence>
                                {showRoomDropdown && (
                                    <motion.div
                                        key="room-people-dropdown"
                                        className="text-gray-600 absolute mt-2 w-full bg-white shadow-lg border border-gray-300 rounded-md z-10 p-4"
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
                                                className="w-16 border rounded-md px-2 py-1 text-right focus:outline-none transition-all duration-100 ease-in-out transform
                                                rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                            />
                                        </div>
                                        <div className="flex justify-between items-center">
                                            <span>People</span>
                                            <input
                                                type="number"
                                                min={1}
                                                value={people}
                                                onChange={(e) => setPeople(Number(e.target.value))}
                                                className="w-16 border rounded-md px-2 py-1 text-right focus:outline-none transition-all duration-100 ease-in-out transform
                                                rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                            />
                                        </div>
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>

                        <div className="flex flex-col justify-center">
                            <button
                                onClick={handleSearch}
                                className="shadow-lg bg-[#32492D] hover:bg-[#273823] text-white font-semibold px-6 py-2 w-full rounded-md transition-all duration-200 ease-in-out transform"
                            >
                                Search
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div >
    );
};

export default SmartOfferSearch;
