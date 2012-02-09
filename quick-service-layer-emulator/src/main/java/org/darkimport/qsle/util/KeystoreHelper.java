/**
 * 
 */
package org.darkimport.qsle.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * @author user
 * 
 */
public class KeystoreHelper {
	private static final Log	log	= LogFactory.getLog(KeystoreHelper.class);

	public static void generateKeystoreFile(final String keystoreFileName, final String keystorePassword)
			throws Exception {
		// Create a java keystore in memory
		final KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(keystoreFileName);
			keyStore.store(out, keystorePassword.toCharArray());
		} catch (final Exception e) {
			log.warn("An error occurred while writing the keystore file.", e);
			throw e;
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static KeyPair generateKeyPair() throws Exception {
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Create a self-signed X.509 Certificate
	 * 
	 * @param dn
	 *            the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param pair
	 *            the KeyPair
	 * @param days
	 *            how many days from now the Certificate is valid for
	 * @param algorithm
	 *            the signing algorithm, eg "SHA1withRSA"
	 */
	public static X509Certificate generateSelfSignedCertificate(final String dn, final KeyPair pair, final int days,
			final String algorithm) throws GeneralSecurityException, IOException {
		final PrivateKey privkey = pair.getPrivate();
		final X509CertInfo info = new X509CertInfo();
		final Date from = new Date();
		final Date to = new Date(from.getTime() + days * 86400000l);
		final CertificateValidity interval = new CertificateValidity(from, to);
		final BigInteger sn = new BigInteger(64, new SecureRandom());
		final X500Name owner = new X500Name(dn);

		info.set(X509CertInfo.VALIDITY, interval);
		info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
		info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
		info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
		info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
		info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
		AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
		info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

		// Sign the cert to identify the algorithm that's used.
		X509CertImpl cert = new X509CertImpl(info);
		cert.sign(privkey, algorithm);

		// Update the algorith, and resign.
		algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
		info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
		cert = new X509CertImpl(info);
		cert.sign(privkey, algorithm);
		return cert;
	}

	public static void main(final String[] args) throws Exception {
		generateKeystoreFile("testStore", "test");
		final String dn = "CN=Test, L=London, C=GB";
		final int days = 365 * 10;
		final String algorithm = "SHA1withRSA";
		final X509Certificate testCert = generateSelfSignedCertificate(dn, generateKeyPair(), days, algorithm);
		final FileOutputStream fos = new FileOutputStream("testCert");
		fos.write(testCert.getEncoded());
		fos.close();

		final KeyStore privateKS = KeyStore.getInstance("JKS");
		final FileInputStream fis = new FileInputStream("testStore");
		privateKS.load(fis, "test".toCharArray());

		privateKS.setCertificateEntry("sample.alias", testCert);

		privateKS.store(new FileOutputStream("testStore"), "test".toCharArray());
	}
}
