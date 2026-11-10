package utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import constants.Tokens;

public class CloudinaryUtil {
    private static Cloudinary cloudinary;

    public static Cloudinary getInstance() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "b429n8kl",
                "api_key", "422734498321135",
                "api_secret", Tokens.cloudinaryKey,
                "secure", true
            ));
        }
        return cloudinary;
    }
}