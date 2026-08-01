import { Endpoint } from "../../../../core/common/apiLink";
import { FailMessage, SuccessMessage } from "../../../common/components/toast/notificationToast";
import { messageConfig } from "../../../helper/message";
import { RequestService } from "../../../utils/response";
import { saveToken } from "../../../utils/storage";

class AuthService {
    async login(data: any, setLoading: Function) {
        setLoading(true);
        try {
            return await RequestService
                .post(Endpoint.Auth.Login, {
                    ...data
                })
                .then(response => {
                    if (response) {
                        saveToken(response.token);
                    }
                    setLoading(false);
                    SuccessMessage("Đăng nhập thành công", "");
                    return response;
                });
        } catch (error: any) {
            console.error(error);
            FailMessage("Đăng nhập không thành công", messageConfig(error?.response?.data?.message));
        } finally {
            setLoading(false);
        }
    }

    async logout(setLoading: Function) {
        setLoading(true);
        try {
            sessionStorage.clear();
            localStorage.removeItem("token");
            SuccessMessage("Đăng xuất thành công", "");
            return true;
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    }

    async profile(setLoading: Function) {
        setLoading(true);
        try {
            return await RequestService.get(Endpoint.Auth.Profile).then(response => {
                return response;
            });
        } catch (error: any) {
            if (error?.response?.status === 403) {
                console.warn("User chưa đăng nhập hoặc Token hết hạn (403)");
                return null;
            }
            console.error(error);
            return null;
        } finally {
            setLoading(false);
        }
    }

    async getAvatar(setLoading: Function) {
        setLoading(true);
        try {
            return await RequestService.getFile(Endpoint.Auth.Avatar).then(response => {
                return response;
            });
        } catch (error: any) {
            if (error?.response?.status === 403) {
                return null;
            }
            console.error(error);
            return null;
        } finally {
            setLoading(false);
        }
    }

    async register(data: object, setLoading: Function) {
        setLoading(true);
        try {
            return await RequestService.post(Endpoint.Auth.Register, {
                ...data
            }).then(response => {
                setLoading(false);
                SuccessMessage("Đăng ký thành công", "");
                return response;
            });
        } catch (error: any) {
            FailMessage("Đăng ký không thành công", messageConfig(error?.response?.data?.message));
            console.error(error);
        } finally {
            setLoading(false);
        }
    }

    async updateProfile(data: any, onBack: Function, setLoading: Function) {
        setLoading(true);
        try {
            return await RequestService
                .putForm(Endpoint.Auth.ProfileUpdate, {
                    ...data
                })
                .then(response => {
                    if (response) {
                        onBack();
                        SuccessMessage("Cập nhật thành công", "");
                    }
                    setLoading(false);
                    return response;
                });
        } catch (error: any) {
            console.error(error);
            FailMessage("Cập nhật không thành công", "Vui lòng kiểm tra thông tin");
        } finally {
            setLoading(false);
        }
    }

    async changePassword(data: object, onBack: Function, setLoading: Function) {
        setLoading(true);
        try {
            return await RequestService.put(Endpoint.Auth.ChangePassword,
                { ...data },
            ).then(response => {
                setLoading(false);
                SuccessMessage("Thay đổi mật khẩu thành công", "");
                onBack();
                return response;
            });
        } catch (error: any) {
            FailMessage("Thay đổi mật khẩu không thành công", messageConfig(error?.response?.data?.message));
            console.error(error);
        } finally {
            setLoading(false);
        }
    }
}

export default new AuthService();
