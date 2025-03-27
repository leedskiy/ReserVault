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
                offerScore,
                hotelStars
            },
        } = filterData;

        const updatedParams = new URLSearchParams(searchParams);

        if (sortingOption) {
            updatedParams.set('sortBy', sortingOption.split('_')[0]);
            updatedParams.set('sortOrder', sortingOption.split('_')[1]);
        }

        if (minPrice) updatedParams.set('minPrice', minPrice);
        if (maxPrice) updatedParams.set('maxPrice', maxPrice);
        if (wifi !== undefined) updatedParams.set('wifi', wifi);
        if (parking !== undefined) updatedParams.set('parking', parking);
        if (pool !== undefined) updatedParams.set('pool', pool);
        if (airConditioning !== undefined) updatedParams.set('airConditioning', airConditioning);
        if (breakfast !== undefined) updatedParams.set('breakfast', breakfast);
        if (offerScore) updatedParams.set('offerScore', offerScore.join(','));
        if (hotelStars) updatedParams.set('hotelStars', hotelStars.join(','));

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
