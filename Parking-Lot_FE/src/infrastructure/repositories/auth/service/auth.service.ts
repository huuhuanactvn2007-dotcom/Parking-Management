import { Endpoint } from "../../../../core/common/apiLink";
import { getFile, getMethod, postMethod } from "../../../core/provider/api";

class AuthService {
    async login(data: any, setLoading: Function) {
        try {
            const res = await postMethod(Endpoint.Auth.Login, data, setLoading);
            return res;
        } catch (error) {
            console.error("Login failed:", error);
            throw error;
        }
    }

    async register(data: any, setLoading: Function) {
        try {
            const res = await postMethod(Endpoint.Auth.Register, data, setLoading);
            return res;
        } catch (error) {
            console.error("Register failed:", error);
            throw error;
        }
    }

    async profile(setLoading: Function) {
        try {
            const res = await getMethod(Endpoint.Auth.Profile, setLoading);
            return res;
        } catch (error: any) {
            // 🟢 Nếu bị 403 Forbidden (chưa đăng nhập/token hết hạn), trả về null an toàn thay vì crash app
            if (error?.response?.status === 403) {
                console.warn("User chưa đăng nhập hoặc Token hết hạn (403)");
                return null;
            }
            console.error("Profile fetch error:", error);
            return null;
        }
    }

    async logout(setLoading: Function) {
        try {
            const res = await postMethod(Endpoint.Auth.Logout, {}, setLoading);
            return res;
        } catch (error) {
            console.error("Logout error:", error);
        }
    }

    async getAvatar(setLoading: Function) {
        try {
            const res = await getFile(Endpoint.Auth.Avatar);
            return res;
        } catch (error: any) {
            if (error?.response?.status === 403) {
                return null;
            }
            console.error("Avatar fetch error:", error);
            return null;
        }
    }
}

export default new AuthService();
