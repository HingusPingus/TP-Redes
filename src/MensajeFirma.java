public class MensajeFirma {
    private byte [] mensajeEncriptado;
    private byte [] firma;

    public MensajeFirma(byte[] mensajeEncriptado, byte[] firma) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.firma = firma;
    }

    public byte[] getMensajeEncriptado() {
        return mensajeEncriptado;
    }

    public void setMensajeEncriptado(byte[] mensajeEncriptado) {
        this.mensajeEncriptado = mensajeEncriptado;
    }

    public byte[] getFirma() {
        return firma;
    }

    public void setFirma(byte[] firma) {
        this.firma = firma;
    }
}
