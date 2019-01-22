package ad;

import com.github.ixtf.japp.codec.Jcodec;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hengyi.japp.mes.auto.worker.WorkerModule;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

public class AdTest {
    static final Vertx vertx = Vertx.vertx();
    static final Injector injector = Guice.createInjector(new WorkerModule(vertx));

    @SneakyThrows
    public static void main(String[] args) {
        Stream.of("E", "F", "G", "H", "I").forEach(it -> {
            final int c = it.charAt(0);
            System.out.println(c);
        });

        final String tokenTest = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoZW5neWkiLCJhZG1pbiI6InBhc3N3b3JkIiwiaWF0IjoxNTQwMjgwODgzfQ.5rnFIqnn9bdBKUuMzxQc0UxS8-SX2uSQIozOwil_NbI";
        final String[] split = tokenTest.split("\\.");
        final byte[] decode0 = Base64.getDecoder().decode(split[0]);
        final String s0 = new String(decode0, StandardCharsets.UTF_8);
        final byte[] decode1 = Base64.getDecoder().decode(split[1]);
        final String s1 = new String(decode1, StandardCharsets.UTF_8);

        System.out.println(Jcodec.password("12356"));


        final long zzzzz = Long.parseLong("fffff", 16);
        final long l = zzzzz / 270000 / 365;
        System.out.println(l);


        final JWTAuth jwtAuth = injector.getInstance(JWTAuth.class);
        final JWTOptions options = new JWTOptions()
                .setSubject("5b384b2cd8712064f101e31e")
                .setAlgorithm("RS256")
//                .setExpiresInMinutes((int) TimeUnit.HOURS.toMinutes(8))
                .setIssuer("japp-mes-auto");
        final JsonObject claims = new JsonObject()
                .put("uid", "5b384b2cd8712064f101e31e");
        final String token = jwtAuth.generateToken(claims, options);
        System.out.println(token);


    }

//    private static void testSearchHr(String q) throws LdapException {
//        final ConnectionFactory connectionFactory = injector.getInstance(ConnectionFactory.class);
//        final SearchExecutor executor = new SearchExecutor();
//        executor.setBaseDn("ou=hr,dc=hengyi,dc=com");
//        final String s = J.strTpl("(|(displayName=*${q}*)(cn=*${q}*))", ImmutableMap.of("q", q));
////        final String s = J.strTpl("(displayName=*${q}*)", ImmutableMap.of("q", q));
//        SearchFilter searchFilter = new SearchFilter(s);
//        final SearchResult result = executor.search(connectionFactory, searchFilter).getResult();
//        final Collection<LdapEntry> entries = result.getEntries();
//        System.out.println(entries.size());
//
//        final DefaultLdapEntryMapper<HrUserAd> mapper = new DefaultLdapEntryMapper<>();
//        final List<HrUserAd> oaUserAds = result.getEntries().stream()
//                .limit(10)
//                .map(it -> {
//                    System.out.println(it);
//                    final HrUserAd oaUser = new HrUserAd();
//                    mapper.map(it, oaUser);
//                    return oaUser;
//                })
//                .collect(Collectors.toList());
//        System.out.println(oaUserAds);
//    }
//
//    private static void testSearchOa(String q) throws LdapException {
//        final ConnectionFactory connectionFactory = injector.getInstance(ConnectionFactory.class);
//        final SearchExecutor executor = new SearchExecutor();
//        executor.setBaseDn("ou=oa,dc=hengyi,dc=com");
//
////        SearchFilter searchFilter = new SearchFilter("cn={q}");
////        searchFilter.setParameter("q", q);
//
//        SearchFilter searchFilter = new SearchFilter("(displayName=*" + q + "*)");
//        searchFilter.setParameter("q", q);
//
//
//        final SearchResult result = executor.search(connectionFactory, searchFilter).getResult();
//        final Collection<LdapEntry> entries = result.getEntries();
//        System.out.println(entries.size());
//
//        final DefaultLdapEntryMapper<OaUserAd> mapper = new DefaultLdapEntryMapper<>();
//        final List<OaUserAd> oaUserAds = result.getEntries().stream()
//                .limit(10)
//                .map(it -> {
//                    final OaUserAd oaUser = new OaUserAd();
//                    mapper.map(it, oaUser);
//                    return oaUser;
//                })
//                .collect(Collectors.toList());
//        System.out.println(oaUserAds);
//    }
//
//    private static void testAuthenticate(String hrId, String pass) throws LdapException {
//        final Authenticator authenticator = injector.getInstance(Authenticator.class);
//        final AuthenticationRequest authenticationRequest = new AuthenticationRequest(hrId, new Credential(pass));
//        final AuthenticationResponse response = authenticator.authenticate(authenticationRequest);
//        System.out.println(response.getResult());
//    }
//
//    private static void testMapper() throws LdapException {
//        final ConnectionFactory connectionFactory = injector.getInstance(ConnectionFactory.class);
//        final SearchExecutor executor = new SearchExecutor();
//        executor.setBaseDn("ou=oa,dc=hengyi,dc=com");
//        final SearchFilter searchFilter = new SearchFilter("employeeID={hrId}");
//        searchFilter.setParameter("hrId", "12000077");
//        final SearchResult result = executor.search(connectionFactory, searchFilter).getResult();
//        final LdapEntry entry = result.getEntry();
//        final DefaultLdapEntryMapper<OaUserAd> mapper = new DefaultLdapEntryMapper<>();
//        final OaUserAd oaUser = new OaUserAd();
//        mapper.map(entry, oaUser);
//        System.out.println(entry);
//        System.out.println(oaUser);
//    }

}
