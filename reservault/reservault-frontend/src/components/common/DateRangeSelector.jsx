import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { DateRange } from "react-date-range";
import { format, parse } from "date-fns";
import "react-date-range/dist/styles.css";
import "react-date-range/dist/theme/default.css";

const DateRangeSelector = ({
    startDate,
    endDate,
    onChange,
    placeholder = "Select date range",
    shadow = true,
    hasError = false,
    disabledDates = [],
    minDate = new Date(),
    maxDate = undefined,
    popupPosition = "bottom",
}) => {
    const [showCalendar, setShowCalendar] = useState(false);
    const calendarRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (calendarRef.current && !calendarRef.current.contains(e.target)) {
                setShowCalendar(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const parsedStart = startDate
        ? parse(startDate, "MM.dd.yyyy", new Date())
        : new Date();
    const parsedEnd = endDate
        ? parse(endDate, "MM.dd.yyyy", new Date())
        : new Date();

    const popupClasses =
        popupPosition === "right"
            ? "left-full top-0 ml-4 -mt-28"
            : "top-full mt-2";

    return (
        <div className="relative w-full" ref={calendarRef}>
            <button
                type="button"
                onClick={() => setShowCalendar((prev) => !prev)}
                className={`bg-white ${shadow ? "shadow-lg" : ""} text-left w-full px-4 py-2 border rounded-lg focus:outline-none transition-all duration-100 ease-in-out transform
                focus:ring-1 ${hasError ? "border-red-500 ring-red-500" : "focus:border-[#32492D] focus:ring-[#32492D]"} text-gray-600`}
            >
                {startDate && endDate
                    ? `${startDate} → ${endDate}`
                    : placeholder}
            </button>

            <AnimatePresence>
                {showCalendar && (
                    <motion.div
                        key="calendar-popup"
                        className={`absolute z-30 rounded-lg overflow-hidden shadow-lg bg-white ${popupClasses} border border-gray-300`}
                        initial={{ opacity: 0, y: -10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10 }}
                    >
                        <DateRange
                            ranges={[
                                {
                                    startDate: parsedStart,
                                    endDate: parsedEnd,
                                    key: "selection",
                                },
                            ]}
                            minDate={minDate}
                            maxDate={maxDate}
                            disabledDates={disabledDates}
                            onChange={(ranges) => {
                                const start = ranges.selection.startDate;
                                const end = ranges.selection.endDate;

                                if (start && end) {
                                    onChange({
                                        startDate: format(start, "MM.dd.yyyy"),
                                        endDate: format(end, "MM.dd.yyyy"),
                                    });
                                }
                            }}
                            rangeColors={["#32492D"]}
                            showDateDisplay={false}
                        />
                    </motion.div>
                )}
            </AnimatePresence>
        </div >
    );
};

export default DateRangeSelector;