import { useEffect, useRef, useState } from "react";
import { FaEllipsisH } from "react-icons/fa";
import DropdownMenu from "./DropdownMenu";

const DropdownButton = ({ itemId, menuItems = [], position = "left-0 top-10" }) => {
    const [open, setOpen] = useState(false);
    const dropdownRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    if (!menuItems || menuItems.length === 0) return null;

    return (
        <div className="relative flex" ref={dropdownRef}>
            <button
                onClick={() => setOpen((prev) => !prev)}
                className="duration-200 flex items-center justify-center w-8 h-8 rounded-lg text-[#32492D] hover:text-[#273823] hover:bg-gray-200"
            >
                <FaEllipsisH size={20} />
            </button>

            {open && (
                <DropdownMenu
                    isOpen={true}
                    onClose={() => setOpen(false)}
                    menuItems={menuItems.map((item) => ({
                        ...item,
                        onClick: () => {
                            setOpen(false);
                            item.onClick();
                        },
                    }))}
                    position={position}
                />
            )}
        </div>
    );
};

export default DropdownButton;