package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.OdklApi;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class OauthHelper {


    private static void requestAccesToken() throws IOException {
        Scanner cin = new Scanner(System.in);
        System.out.print("Application ID: ");
        String applicationId = cin.nextLine();
        System.out.print("Application secret key: ");
        String secretKey = cin.nextLine();
        System.out.print("Redirect URI: ");
        String redirectURI = cin.nextLine();
        System.out.print("Scope (VALUABLE ACCESS;SET STATUS;PHOTO CONTENT): ");
        String scope = cin.nextLine();

        OdklApi api = new OdklApi(applicationId, "", secretKey, "", "");
        System.out.println("Visit this page and authorize access:\n" + api.getLoginUrl(redirectURI, scope));
        System.out.print("Paste the code from the query string after redirect: ");
        String code = cin.nextLine();

        System.out.println("Tokens:\n" + api.authorizeApp(redirectURI, code));
    }

    private static void requestRefreshAccesToken() throws IOException {
        Scanner cin = new Scanner(System.in);
        System.out.print("Application ID: ");
        String applicationId = cin.nextLine();
        System.out.print("Application secret key: ");
        String secretKey = cin.nextLine();
        System.out.print("Refresh token: ");
        String refreshToken = cin.nextLine();
        OdklApi api = new OdklApi(applicationId, "", secretKey, "", "");
        System.out.println("Tokens:\n" + api.refreshToken(refreshToken));
    }

    public static void main(String []args) throws IOException {
        if (args.length > 0) {
            requestRefreshAccesToken();
        } else {
            requestAccesToken();
        }
    }
}
