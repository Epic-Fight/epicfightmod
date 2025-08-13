package yesman.epicfight.client.renderer.shader.compute_boost.compat;





public interface IIrisCompatContext {


    default short getEntity(){ return 0; }
    default short getBlock(){ return 0; }
    default short getItem(){ return 0; }

    public static class CTX{
        public static IIrisCompatContext INSTANCE = new IIrisCompatContext() {
        };
    }


}
