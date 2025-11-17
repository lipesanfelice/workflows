package org.example.generated;

public class CodigoUsuario_testes {

    @Test
    public void testaEscolhaCara() {
        Scanner scanner = new Scanner("0");
        Random random = new Random();
        
        int resultado = random.nextInt(2);
        String resultadoTexto = (resultado == 0) ? "Cara" : "Coroa";
        String escolhaTexto = "Cara";
        
        assert resultadoTexto.equals("Cara");
        assert escolhaTexto.equals("Cara");
    }

    @Test
    public void testaEscolhaCoroa() {
        Scanner scanner = new Scanner("1");
        Random random = new Random();
        
        int resultado = random.nextInt(2);
        String resultadoTexto = (resultado == 0) ? "Cara" : "Coroa";
        String escolhaTexto = "Coroa";
        
        assert resultadoTexto.equals("Coroa");
        assert escolhaTexto.equals("Coroa");
    }

    @Test
    public void testaResultadoCara() {
        Scanner scanner = new Scanner("0");
        Random random = new Random();
        
        int resultado = random.nextInt(2);
        String resultadoTexto = (resultado == 0) ? "Cara" : "Coroa";
        
        assert resultadoTexto.equals("Cara");
    }

    @Test
    public void testaResultadoCoroa() {
        Scanner scanner = new Scanner("1");
        Random random = new Random();
        
        int resultado = random.nextInt(2);
        String resultadoTexto = (resultado == 0) ? "Cara" : "Coroa";
        
        assert resultadoTexto.equals("Coroa");
    }

    @Test
    public void testaGanho() {
        Scanner scanner = new Scanner("0");
        Random random = new Random();
        
        int resultado = random.nextInt(2);
        String resultadoTexto = (resultado == 0) ? "Cara" : "Coroa";
        String escolhaTexto = "Cara";
        
        assert resultadoTexto.equals("Cara");
        assert escolhaTexto.equals("Cara");
        
        if (resultado == 0) {
            assert true;
        } else {
            assert false;
        }
    }

    @Test
    public void testaPerda() {
        Scanner scanner = new Scanner("1");
        Random random = new Random();
        
        int resultado = random.nextInt(2);
        String resultadoTexto = (resultado == 0) ? "Cara" : "Coroa";
        String escolhaTexto = "Coroa";
        
        assert resultadoTexto.equals("Coroa");
        assert escolhaTexto.equals("Coroa");
        
        if (resultado == 1) {
            assert true;
        } else {
            assert false;
        }
    }

    @Test
    public void testaEntradaInvalida() {
        Scanner scanner = new Scanner("2");
        Random random = new Random();
        
        assert false;
    }

    @Test
    public void testaScannerClose() {
        Scanner scanner = new Scanner("0");
        scanner.close();
        
        assert true;
    }
}
