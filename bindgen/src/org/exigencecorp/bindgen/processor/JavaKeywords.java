package org.exigencecorp.bindgen.processor;


public class JavaKeywords {

    private static final String[] javaKeywords = "abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,strictfp,volatile,const,float,native,super,while"
        .split(",");

    public static boolean is(String name) {
        for (String keyword : javaKeywords) {
            if (keyword.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
