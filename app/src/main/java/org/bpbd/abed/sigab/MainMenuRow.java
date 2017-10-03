package org.bpbd.abed.sigab;

/**
 * Created by ABED on 14/08/2017.
 */

public class MainMenuRow {

    private String image;
    private String jenis_bencana ;
    private String foto;
    private String nama;
    private String key;



    public MainMenuRow(){

    }

    public MainMenuRow(String image, String jenis_bencana, String foto , String nama, String key) {
        this.image = image;
        this.jenis_bencana = jenis_bencana;
        this.foto = foto;
        this.nama = nama;
        this.key = key;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getJenis_bencana() {
        return jenis_bencana;
    }

    public void setJenis_bencana(String jenis_bencana) {
        this.jenis_bencana = jenis_bencana;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto_profile(String foto) {
        this.foto = foto;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }




}
