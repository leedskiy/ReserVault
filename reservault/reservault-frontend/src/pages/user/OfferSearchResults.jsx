import { useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useCallback } from "react";
import { useAuth } from "../../context/AuthContext";
import { useNavigate, useParams } from "react-router-dom";
import api from "../../api/axios";
import SmartOfferSearch from "../../components/user/SmartOfferSearch";
import OfferList from "../../components/manager/OfferList";
import Header from "../../components/common/Header";
import OfferSearchSidebar from "../../components/user/OfferSearchSidebar";

const OfferSearchResults = () => {
    const { isAuthenticated, isUser, loading } = useAuth();
    const navigate = useNavigate();
    useEffect(() => {
        if (!loading && (!isAuthenticated || !isUser)) {
            navigate("/login");
        }
    }, [loading, isAuthenticated, isUser, navigate]);

    const [searchParams, setSearchParams] = useSearchParams();

    const {
        data: offers,
        isLoading,
        error,
    } = useQuery({
        queryKey: ["offers", "search", searchParams.toString()],
        queryFn: async () => {
            const { data } = await api.get(`/offers/search?${searchParams.toString()}`);
            return data;
        },
    });

    const handleApplyFilters = useCallback((filterData) => {
        const {
            sortingOption,
            filters: {
                minPrice,
                maxPrice,
                wifi,
                parking,
                pool,
                airConditioning,
                breakfast,
                rating,
                hotelStars
            },
        } = filterData;

        const updatedParams = new URLSearchParams(searchParams);

        const sortingMap = {
            "Price (Low to High)": { sortBy: "price", sortOrder: "asc" },
            "Price (High to Low)": { sortBy: "price", sortOrder: "desc" },
            "Offer Rating (Low to High)": { sortBy: "rating", sortOrder: "asc" },
            "Offer Rating (High to Low)": { sortBy: "rating", sortOrder: "desc" },
            "Hotel Stars (Low to High)": { sortBy: "stars", sortOrder: "asc" },
            "Hotel Stars (High to Low)": { sortBy: "stars", sortOrder: "desc" },
        };

        if (sortingOption && sortingMap[sortingOption]) {
            const { sortBy, sortOrder } = sortingMap[sortingOption];
            updatedParams.set("sortBy", sortBy);
            updatedParams.set("sortOrder", sortOrder);
        } else {
            updatedParams.delete("sortBy");
            updatedParams.delete("sortOrder");
        }

        if (minPrice !== undefined) updatedParams.set("minPrice", minPrice);
        if (maxPrice !== undefined) updatedParams.set("maxPrice", maxPrice);
        if (wifi !== undefined) updatedParams.set("wifi", wifi);
        if (parking !== undefined) updatedParams.set("parking", parking);
        if (pool !== undefined) updatedParams.set("pool", pool);
        if (airConditioning !== undefined) updatedParams.set("airConditioning", airConditioning);
        if (breakfast !== undefined) updatedParams.set("breakfast", breakfast);
        if (rating?.length) updatedParams.set("rating", rating[0]);
        else updatedParams.delete("rating");
        if (hotelStars) updatedParams.set("hotelStars", hotelStars);
        else updatedParams.delete("hotelStars");

        setSearchParams(updatedParams);
    }, [searchParams, setSearchParams]);

    return (
        <>
            <Header />

            <div className="container mx-auto py-8 max-w-6xl flex flex-col gap-6">
                <SmartOfferSearch />

                <div className="flex gap-6">
                    <OfferSearchSidebar onApplyFilters={handleApplyFilters} />

                    <div>
                        <OfferList
                            offers={offers}
                            isLoading={isLoading}
                            error={error}
                            onModify={null}
                        />
                    </div>
                </div>
            </div>
        </>
    );
};

export default OfferSearchResults;
