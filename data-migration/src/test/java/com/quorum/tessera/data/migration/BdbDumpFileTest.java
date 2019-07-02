package com.quorum.tessera.data.migration;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class BdbDumpFileTest {

    private BdbDumpFile loader;

    @Before
    public void init() {
        this.loader = new BdbDumpFile();
    }

    @Test
    public void loadOnNonexistentFileFails() throws IOException {
        final Path randomFile = Files.createTempFile("other", ".txt").resolveSibling("non-existent.txt");

        final Throwable throwable = catchThrowable(() -> loader.load(randomFile));
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doesn't exist or is not a file");
    }

    @Test
    public void nextReturnsEntryWhenResultsAreLeft() throws URISyntaxException, IOException {
        final Path inputFile = Paths.get(getClass().getResource("/bdb/bdb-sample.txt").toURI());
        loader.load(inputFile);

        final DataEntry nextEntry = loader.nextEntry();

        assertThat(nextEntry).isNotNull();
    }

    @Test
    public void nextReturnsNullWhenNoResultsAreLeft() throws IOException, URISyntaxException {
        final Path directory = Paths.get(getClass().getResource("/bdb/single-entry.txt").toURI());

        this.loader.load(directory);

        // There is one result in the database
        final DataEntry nextNotNull = this.loader.nextEntry();
        assertThat(nextNotNull).isNotNull();

        // There should be 0 results left in the database
        final DataEntry next = this.loader.nextEntry();

        assertThat(next).isNull();
    }

    @Test
    public void dataIsReadCorrectly() throws IOException, URISyntaxException {
        final Path inputFile = Paths.get(getClass().getResource("/bdb/bdb-sample.txt").toURI());
        loader.load(inputFile);

        final Map<String, String> expectedResults = new HashMap<>();
        expectedResults.put(
                "HKXNiJCf/A6S5Udpb/1STmthoyxAVe8EVLoZgpG0ChXqdOGKFL6bdlQ7yqlqS5ssy3n39SEMSwhgWwbcN3SMxA==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5ADzypKqF2AvCgvu/Va4+xX49f6qJ6o3fKL/YymZ3+9EbyFQRjs79b5dLxvk+ISlU02dCs/4vBswguXLjRnOc1fz29GeW/ztMXpX2t3xYfO9EIBbQTQhN/d3jJtdAuADeC9niZbCLoG2TCFq+dvIhxgj69qmIuyEvr73mXpyMNtioCgUxcYQtAYHAu6EdhDz/1DBegzxH4HP9Swpszh3C95t1lZxa7VMOr0rvGHy1dCOv+ifU4jWrKbvDXuP8Lu3Dk0QWNevZDy1BopsmRTLP4KI3rDAuREsusC5sX++vlkEapdfzAc/pIKVmgEM9UbUT/u+76Q0QOBpp4ZJ8zcFTJcIvmM3OXbal53l8DLCMqhONDzh8gjKIH13PgCsB07t5Ry32hlfx2spmGMXjdcpyeNy0jwxNVcWQqJjzZagdPXljTxak2WkOP+WojRPS98fKQZw1C6eOMnVeaM0+VC5P/xZy4dGQHRe7bm9FKe3Nqivgmsq8xUFEHdkAAAAAAAAAGOERIG3ZLsDF5Z8xof/c6L/+dgEVa6BvTAAAAAAAAAABAAAAAAAAADAyuFOM59OpRi5Z64Z+kvXMD2fI3pkCQEZf+k04fi2ifpPHKjMgHAZUwVOIiqwPbAEAAAAAAAAAGDGq1EYzhmzI1G1vj9VBKRB9DaLK4ypXQAAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "UwpFurSYDtOdGuvUYpNgGAfqiyi/UEAS/p52ghtrrPKNd5tpNU6IHUuc/wBziDfkLNmZsrj3E62R0MrbTWgQTw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5STTjPzu4JCF37Zi/ce8mhgDGAkKR3sppszFPviHiyv4e7UpZKoimu6WgcqrjUT2XT9MQU9+XmhIXzZFuZr7h/5/t7X3VFAEj6rzPzWy6tskxrlMwzY2/4bvQEFa5jpX0hhe9oBdVHDALePQlW/YEOQZzBk7DY6f/hC+tG+AWkD2dq8rN/v+A3TNnlcGELG8lMXQdM56afyJhA0+LVAPK9juwRMQGo3b3hU6tBBJ0AeaePGEuQ+u4O9ShhUI8JLVYCblwEcDb0mwknJMEHmGQ9IIxQFCNthQNdAs2/24pe86UMTBHZQILlvLlnLfaCHk1iQP3ngkz2jHACgFMPp4Q+LgrHKynmQ0GgtYh/Ql6ruLSwA+mRq9B3y9MtL3InEonlwsxyxYXphHPnM29dJX6T5YNFCTnhun9U1y5BKnAJHtQozuOQ0CTNY5AtTXkMa5N1Qu07mon0f4ARJXCZrdWvbhaHJ2D/G5P59/Yg4NwS1Cf+/l7mfFC/ZkAAAAAAAAAGG4SKlQ/mPVrtgPanKrYNKxvyG1KTkwB3wAAAAAAAAABAAAAAAAAADCrnyHwAWWOzaQswC8zGBgOkIirnaP8LKhPa2DtSNyNoYo3EX7sC0PNfazsYrH7TYAAAAAAAAAAGO+q+mBo55A/27pnUfH82BjdX8Aw39WyygAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "v3flCFXKivpmNgerfvAqdRGn/ktrS9bSz3W/2NfksokVyaEzzR7+INjgvUwv+JW5U53SUz+X8Eq0ys33hWoIvA==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5lH8PaSlFuRWMzOTpk2ftJkSdW8Gfy7M/Eam+gcaAfiT04YAS8SQoSgze4k7h59jub66Wyl02qFktU9gTV5uQzKegEoyIWOBbKyc3+kqIFcucYgxjy9Z8cFv/3swBxMVUHD20K9JR9vbbUxx92I3Z0pcD2uNC3TeVso5/q5lrfgJxWK4IrwuKM7tXU9BvfCkckF6jAn6uGnRtxcJAN+urCKZ+JhOMMKAyPsyklYI0oEAVYZXOf25k6mM3wcDvJ1K541TOIE/7HSXR9Uds4Y+YUAFjlJWVPHIYMp+CGOSDFI5+clXVumcSkz4SW4j/M01KTS4B72+pULYm7GhlhyDY8Z8mhM/L4/jVvfR/5ee+O2dWioAAj/NuQ+9gtXyAG7fs1nX89ZlnsFVasJzDaIRYtqp8LxfnFsW6c2rubRFiSyCc0Xo/1rPQmn10uALGPuSgjjg8adtrd8BWrwic8IHpquNuLVxF5NJ0ePl6MbBQf3KNOCoGI9e+N/sAAAAAAAAAGN7LyZlOCTXQjYK8hKhUI3SXPN1azeIyrgAAAAAAAAABAAAAAAAAADBSU6Im7no5mJIVZJIfrRR0FK+ny9lEpBpe2QMx4p0WMrPgXbiqkYY4AlTzI/Lz4AIAAAAAAAAAGD3XeaAqccddPKxDw/TqZHrsgbGFJ/jbMgAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "u27yeDLWnchRJh0VjSrt6MkhgiwkO5/l8xeNLC+oyM8CopzcY/a1TSUjunES7qTFY5hkunZtTtRklFW9dGM6IA==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5YWfCOqGYTSgYxo49c5O4Oii1a4nxFuKyz7+6ar9XpcmkW+ek5fY5Gb8tQmAOMBDjD4WXYweZjMAQS5o32T3EKXQNwha2tLWcJKtd2k2H4nZ8/WYfh0Z56lTl6Yz6JEzPUeOyv1JvBx9MLPjAhZZxCq5q2EyY8OLy+3/auuorGiZfWc99xZYtPdIAsD7GfZADY+5WfeXSsQ+4b73IwCVsk3g1XTf1OJZQQCSIObCWWMr6YuaeX2Mqj9J3SKvjpZ7ExaPBY0IY5bX+6Wtv8Nkdvluy5iKc28GHL+ndQkFgHKWifCmOnIvekjiNqwJds+WbHT3nzTc/LJKyXbNt/E0Oab7GszgzoUWn5StNdxM85fWXXM/KKgmInrEBZGv6oJPCp17tpdDHfrweYeHPW+NRCFNVo3H2pzHcQRN7YyXd//rX16Er7l1p1KUAPBJVvvCHAwHIkU7a2fh15Zfc4mgsWRTZA2TdGV8u3haxG4iVOLVqe7+4uEv2d0sAAAAAAAAAGF57bHvwhZY3/9x7qge4D9lp5j/sLt1IAgAAAAAAAAABAAAAAAAAADDFx1t5IO0G9eebRNNf7pRSFjL/jH83HyU/0vJgFtmpOtzYAKUkYWUyv/V5d+n3pDoAAAAAAAAAGPlESaPcUGaWzhNR6Hp6RSYnWnJqu0BpmgAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "uyygUu0BBIW0KXDL2XmS2sT78ExMqxkw/DtOA/HoJH1LRWO/Lh64J2n/RwvCJnpbisgHWGVkpSVmA1PjzyKiTA==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5k9fu9ZaGlwtS/1bYJtS8m0yZhoYONFORGzw66J8pS0FfZ0MKfDELsR66jgC3ZLk2gkcC70B5xExphrHoO6SDjW/P/vbyTNkbzhZ6ppB8jHxEdI6rXkYoqmoUoe+JimUd0fnm+WltJn8D8XFt74HjBn5/gLOBJgX/edVn03WRZL6LJ4gdgm748HNDPEYj82ryMj77avuCZHTQt+hFKE8ZeznZqZKXQje31xM9a/Mj87guHTpYBjmobxC1HTdwPOFNFIAdneRfodBKS5EV96mZZlEKA2fGPGO1MQkUKwRostbHHeoM6zSV+rBhDr72Lwy649//cgxezlTkn2campu369+St6kJHAox6bHCccADQQxb48tgORiNexgamPnDSLD80+UtMMDjNMJf64MqhofPybZZF85cgkVA9YCcWBX25+K3AYJzwh450yf3hyuZTUzcVi+eEhyZKrcWYR4vcw/rYJ39KaLH2dsrHw9NMWqB32keqisQQGrSyVUAAAAAAAAAGDgGuptgsOrKrergfKCkEnUzqjLlF+YLYgAAAAAAAAABAAAAAAAAADC1P9wYRAlPr2k5OPt69IBu1y8chhGDdWLxtb3QZa5Ao768DYZoJkNjvUrEPYzyZn8AAAAAAAAAGP4DuoZ/j0xQILUvhEH4KWyGU3QFIuopCQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "U0Wo/9fX8YJrM8azM00+0CX6aUAlx8f8HgTccnmn1grmd1IK0fgVCApZe9dMc/gOu1f+hPZTqMFzfgC2RgHNUw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF50ubufyX+rMi5GgNmwyb/JWkCClYGdUVJW1FEahdKDGjBP4lf8K7eZVkm7QgXuloF+fEX+KgvSGmZ3gpt0HKB2ikMA0hxyKa6fOd/PGRffx+4mxr092w2AnwWNwl7NvAzHOeanOlZ8VYWnMGS/uD/DIxm1VwCabK3b4XFiuAvwSlIuCO8LU1u6I+W4dYNhTYtU9rHdGusFuLPVCcR7LWG+knDRsu/6g0XK5sXEB//7fiiieSBmysf5BCyqi8qFXN/ryzf9LazbwB5RkNRSlp08rVSkonpVEo94b65ljx/j+ZJzpDTUiW8zyi3y1W5UiB1Ga/z4tCKrn3BAdKNmCAC/4So7LNseylObKhBVELYT4o/k6vMCJ/PV+XBS9MzB3S8EFk1Doc1JvB60ZLtSGavDQ3kmSfmJPHDpcCddt7TiSE5XHdf7xMyLolYhc+8l0rxZkrtHUuO3sr6b3oCN2M64XsyrIBHTxPYXAdL4Y/E+HlpW4FFas/zpd4AAAAAAAAAGI6ALzEGuZG0nPBxggNrNwEr+tWYgIPbHwAAAAAAAAABAAAAAAAAADCR1+A7p7vN5UBKp8GfNgz2mG+cngQiQ0nH0g9k69by1UhAgdRx9lJpr3o9zhxsyKQAAAAAAAAAGJItLLQRF7QAtXBGYWy6tCBk0r1rp2JAqwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "SLy4i4UtWA2fodqBCa7hQZPE6H4lnM4iDDaesYMKBsUvVjxcPyHkEJ6hLom5AHkX8tprRNQaBFMAJ4pVtLgm0w==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5rRQ+YoUKJD8jcOqJJGcXm08kGlOFD2YQ+93b1SZ2JMrd2BQZ8UVOQoauhEGVzNgSY17sTvV0un4URWW40LcEB4w8qAlacTc+O7UQmVGSiwyj5kOXeSfFMVuwNHLO7r4Y7RZkXjaNBlNnF+RtDd6PcALNAOH0bA2ZEQ00epDE7mOgrMoER1uPdt4q8zqZcewI7oI2AFJmndo32+bR++zS9RvEuhjR1vsQKilrK9QVg3EEt7BSSt+0FHy2eOky7+xsRW/chiUTazm6pbYDVcjK1gjAbQ/uFLMinEN2ohmK3LgT70bH2JlDuyAlbRLoITUSGIM60xsqP1Q8/kYLDkwuZEuSjDK74uhiWDLm3GNkX0MX/LVRscvIqzpjnTZCxeSjRBsYd4i106UsiUS5PPUiJ6wIXCt5hTMu2NQSqMh1oD47g0S5nWIjcpCJ5xW7oUtOU0S/H44k4KmRtBjJIQqI3+2BufYTPDfwn+MuO8lHac1e+gke5PNwCxIAAAAAAAAAGJ2DAzlk2Qf+9xcPr78UBk7T3sz+tJC/EAAAAAAAAAABAAAAAAAAADB50sZI2CXDR/I2JC6wiYkrHsgJlvENQqhbm5cwhziYUIKnCLyr4PB8TpNuopBa7pgAAAAAAAAAGG6omNKZ70rC+rJIstyVUjA8UdL2KEyelgAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "a9Mv7emST/mkM9HTs1Mh8SKgow7wDru11GeiOgNUt7zN20OTS6Pcs5+3UN3FyLm/GhBNInORd5JCNKs3PnTdJg==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5md08Db1uK+zbybuPGQDUBZeJ2Oyl8Sl8ZNjvOcR2Z2gw+pYiptx+NTmz9KGEz7gI22iDz3FqnNQT8EUPKuLRoDAwsXm6+yGGUxsOH3PTiw1VreAV+720wx2wf22IzonjmhR/Q9H+Uh3axNbuSYEwER93N6oiccCecVyUaGGuXCjXRrae5ppnjNRj+JwbG8rqtnqD1OATYwl76UcRHjbv+CXvqBHenEEfHTB9InaOlrxz2ieU21fjc+/JNWSzLIG3uk5q7fsWGEMUlGJjJizu1x14wxqx7z2KzkSUTv1qlNAIPs5L1Rhm7957Jr04Fq7okSqszz5HgGcCYwa2xbQoc9KziKmrAc/ZFuVi9QS0w9WoAv1QYLHRgBi1zzZkB/bNdERboNtFx4bm2JAnvN17S5GbNRziFjhjzUnw/qxAzWgWD8nLizEpUecTtFIrbxB+QsRbDPwqVJW/7gchhTEb4j+FR035yfA/O9Aj3aw6tOjc5d5Jfd2W0uoAAAAAAAAAGB9agi1OAWwTIO4wR9846XZlIwDKPOemhAAAAAAAAAABAAAAAAAAADB2naJU2ADYjQdhgJ26dMq506vamjiNStLlWqVtoNttS4o/UWtnlI1GoRlQtjrbdXwAAAAAAAAAGLOhp1YxQQR62qbLojm2CxQbYFQZKt/pMAAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "VMzOxFeeBl6ET+wDKcPK/RD+lUt1+1Qvd8fFGqTqD2nj6VhDH9cMCuPR640VVpvG2OfrAUuTmNJzLcNcr0reWQ==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5ZboE+DUHw/a30Zu314IhKj4TStZSaJwZHL+Ytc5rmnTqwjyts+VDNBxGWIcMk4ihC/Oki+KGtBRzQBcLAXufjUIVGSHvkzle0qScl07r2oRZaUvWPFi66Ywj9ginNDVXt5P+wreQdAhXQeQTfPSiXDkUqWFu0VcKWEBHsm2vQpgImuQ7bvssW0C7alTaCrJGPqyc5Wl5no/PhHcx7T8+fEkZB7f7slQOe5nnpjWRSkiVoq1/fOYRHc9N9d9u7IVysCg5L3KCRrZfypip5RIkGV9UhapODtTryWDxViuII0v+stJlm7s8rV7nMSw9kbufKzockqgiWd8oTqRudn5jSWz/aadqa//bOO9BndPnOhEVAi7SjpVlUPnfTU5lsrTPwL7Rauu/IzW3UyuKoUBZDE4zF7BGogzNBMKLkuunOXWcmRQl0SdspFr7GqHfx05tru44oFsbke8gir6AxuS2kkxFTJU9FD/SkMvhj6sGseKnQA5ErdE4Ju0AAAAAAAAAGPO72nGXCWmVNflyprA2O+PHUIw0CZxH7wAAAAAAAAABAAAAAAAAADBySWt1MtnW2ydPWc0yf9iqtZbIQQUCo3Uur5y9R68D4Fh+H2UfG/p6YOUHh9fR0sMAAAAAAAAAGNK75NAkWcmpZ96TiVe/NCIYD6xkYySFXAAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "CwzlSMoYZtDwYjJEAXqVljJLt0I6jsd/mNdo75dwrw/VmMMb5hOKD9+b8tbJR6G4ZDAqjWYZp2SyCZKj6lYJiQ==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF53RJG0/xA/viFr54JiimBddj7TIcfbXSlKy/g4ALEz2ofZuClHfNuOG5pgPIM3fUCNmmiv6POtH+kH1nWMtgyQh3+d9O/BaNvg9xJJXwlHG/R4zhIdd687h9NkFpg+D0+u2j4uxaQh05yQid/FJO3Tii0g/6MOXIEZJ+txYJ4DIuzuHuRIfOFm0AHdfrsAQRqyFitVfNelYrqeK+u0ImClbqBqtar2bGzQi6YfEJXwYXZSF8fM7khh2R0tvsHfdf97PGIhu+0Pfy4n+eItx4PZ2d6XFYaCnbEkIT0eKSb6aFsB89+K5KNjrlaY7nH+W4Gv/MLZclnyUeXvq5AQoQeRvflqRIHQwG+7MTMG3OBc/TP1SvPrZeheO5gLDlcYRu5CzUBtlOV8mAy0xYXMwSE/AmJNCQhFrYth440OJ3SBOOazGflgKYtn3wlU+RaAxbk/VF91Fj/jR1vw/M9bNpM9NFZhiSUqAAariUPMDXFULnGJCgddwUm+QAAAAAAAAAAGAA4MOexkYdUOgjCttEkbJ2xxifvOPOmUQAAAAAAAAABAAAAAAAAADCjWCnI0Zns59UjONAn4QqsWOetlwP2gG18vKAoI1aKr2eImZ8V4SLuveiEg71AVjkAAAAAAAAAGL0C3PLkT5xURmE7hjoS5qvd47Mkuks3yQAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "OUCqgB+VFW3j6tZcb5QQAIYPBa5Hqfl0zW8GHN+3rFgqzoSFJcuwUb1lYbVkfedMk/2Y6FOpPqepZs2+VWGFAw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5kuFHSPzQlr7CvPSiEAOjEsoaL9nDyJ6KonXaUE4o81YE5kUdcZW5oGK8bT6BcH552e5tlynaoRNj5mKlZSbz3i86U8AOvxdMq8INnpbRaD65mjoq3pRdHPpsoum/BksOD35/7IGt0zczMpwkcvrmu2mxk3eFQhx9gK5lsWuOz6GZ2faaISR0HaIwpzpnb66AYQfSxFeebXQKDLvg8yxOwRBfZAw10lqlCwLctreTJWz0cucNuL69CGJ6dh6LoUOs27YYXwSW/eBsfjYwDtjCvt+elY6Qdm7IOQ8pRLLNsoA32xhNqJuRCT3cBE+NQFrjhrInRIujjmkW1p5b9sbKASL0Z09gP+I1eeujc0hM2x1MdW96AEWQ2DKoarbIsBxlI22pMtcmmTbdQU/r2oTeFz1aWNJmyRcEL544Ae4Z2EZ3nz/uTFnE8bulfpMUd3EtJ8JbrzFJdKWcJr8zeuk4aUpxSvNLHd8In+SYL8WEaNNWKtf56dAmB5QAAAAAAAAAGGItCRTi7O5zd8dAXqOSge/24aSwjInIkQAAAAAAAAABAAAAAAAAADDOIxNUG+u7Jnu6C/S9QG9hgnOSRg6wwjSTrvoMKvkMVyEZpb1zbiqZCTQ6KX33yEAAAAAAAAAAGPTiRg2Ivhg0bMVXGOrv18ih1hDthJmVWwAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");
        expectedResults.put(
                "whA0hI7i3RC60lqlwJacz8qJ6pkDU9JqSCVRs8CTGr/pps9b8bBbk7OWUgq05tZ6PbGsjLTh9z5e+ccRjg+nLw==",
                "AAAAAAAAACAFQt5HwnJRaGK64IxT8csDRDmnORhP5wcgjdkoF7LcGgAAAAAAAAF5b+W7dq5NUwpXSsviDLtQlCIu6royEy+9p5yZ49PfTmhGb+BZ9Ywyx6xVpVZeOVyTlPYIx0FxXmvGDKZ9Tp+8uEL+9eUdun5TdFj7XiAeZ3FnUYQGYgkf6wwCnZVWLpkpoT//dvW9J3GaTYMhAKBKRIbE9cALqRQLNqSQDi8psdKcno/3uqkhT0zrwEbwhA4VMLn9d08L1tp0Y1aHuAJR9Kl8Spr3mdpXKu7cwihPiVdPpaCBqjKNep8zhpuJFBsqAFwrTligfs+mFwCghwbtx/MESDU8use4NkVf3ydC/Kz0kdV3Mfk4r7Ki3nIrjhcqnmWll57CMjn8Glre380/ENJjI5qw/XV4WUXXmNwu+BU8TY2rydIE/ZiRnU4Rg8uwBSvKPNGmj0TTZHIZHv96hrN2nzYYnuVaSqTCEvNpspfIKnlhGZsA5vvnuc7G7VM4TOAloGJpIWBrw+KLevRMyshaGMU0tWCQ+0VFaT0YJMiSm0IgCgSnAUIAAAAAAAAAGEmaK+26w+6u5vQAgTOCpbW3cm/1eUl0ogAAAAAAAAABAAAAAAAAADArrfXnZRKfKOPRfuMY+6V9lS0FjLk8i0B7lcw5W/hqtFPDXqPYqI44xFn18AImJ5UAAAAAAAAAGIeza0xHvdL92y0djJSt+npHl9GXz9/urAAAAAAAAAABAAAAAAAAACBE4BkFa1JpzFdCs57cUYCokPImMV49Hlx7hNIjOYnQFw==");

        final Map<String, String> results = new HashMap<>();

        DataEntry next;
        while ((next = this.loader.nextEntry()) != null) {
            results.put(
                    Base64.getEncoder().encodeToString(next.getKey()),
                    Base64.getEncoder().encodeToString(IOUtils.toByteArray(next.getValue())));
        }

        assertThat(results).hasSize(12).containsAllEntriesOf(expectedResults);
    }

    @Test
    public void emptyDatabaseIsReadCorrectly() throws IOException, URISyntaxException {
        final Path inputFile = Paths.get(getClass().getResource("/bdb/no-entry.txt").toURI());
        loader.load(inputFile);

        final Map<String, String> results = new HashMap<>();

        DataEntry next;
        while ((next = this.loader.nextEntry()) != null) {
            results.put(
                    Base64.getEncoder().encodeToString(next.getKey()),
                    Base64.getEncoder().encodeToString(IOUtils.toByteArray(next.getValue())));
        }

        assertThat(results).isEmpty();
    }
}
