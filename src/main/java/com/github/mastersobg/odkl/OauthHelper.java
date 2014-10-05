package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.auth.ApiConfig;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class OauthHelper {

    public static OdklApi api;

    private static void requestAccesToken() throws IOException {
        Scanner cin = new Scanner(System.in);
        System.out.print("Application ID: ");
        String applicationId = ApiConfig.APP_ID; //cin.nextLine();
        System.out.print("Application secret key: ");
        String secretKey = ApiConfig.APP_SECRET_KEY; //cin.nextLine();
        System.out.print("Redirect URI: ");
        String redirectURI = "http://google.ru";//cin.nextLine();
        System.out.print("Scope (VALUABLE ACCESS;SET STATUS;PHOTO CONTENT): ");
        String scope = "VALUABLE_ACCESS";

        api = new OdklApi(applicationId, "", secretKey, "", "");
        System.out.println("Visit this page and authorize access:\n" + api.getLoginUrl(redirectURI, scope));
        System.out.print("Paste the code from the query string after redirect: ");
        String code = cin.nextLine();

        System.out.println("Tokens:\n" + api.authorizeApp(redirectURI, code));
    }

    private static void requestRefreshAccesToken() throws IOException {
        Scanner cin = new Scanner(System.in);
        System.out.print("Application ID: ");
        String applicationId = ApiConfig.APP_ID;
        System.out.print("Application secret key: ");
        String secretKey = ApiConfig.APP_ID;
        System.out.print("Refresh token: ");
        String refreshToken = cin.nextLine();
        api = new OdklApi(applicationId, "", secretKey, "", "");
        System.out.println("Tokens:\n" + api.refreshToken(refreshToken));
    }
}
