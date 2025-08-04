# Convención de Changesets Liquibase Multi-Tenant

## 📋 Convención de Nombres

### **Formato:**
```
NNNN-[public|tenant]-descripcion-funcional.sql
```

### **Ejemplos:**
- `0001-public-tenants-table.sql` - Tabla de tenants en esquema public
- `0002-tenant-schema-initial.sql` - Esquema inicial para cada tenant
- `0003-public-tenant-settings.sql` - Configuraciones globales de tenants
- `0004-tenant-memberships.sql` - Tabla de membresías por tenant
- `0005-tenant-payment-history.sql` - Historial de pagos por tenant

## 🔄 Orden de Ejecución

### **1. Changesets PUBLIC (context:public)**
- Se ejecutan **PRIMERO** en esquema `public`
- Contienen: tabla `tenants`, configuraciones globales, metadatos
- Numeración: 0001, 0003, 0005, etc. (impares por convención)

### **2. Changesets TENANT (context:tenant)**
- Se ejecutan **DESPUÉS** en cada esquema de tenant (`gym_001`, `gym_002`, etc.)
- Contienen: tablas de negocio, datos específicos de cada gimnasio
- Numeración: 0002, 0004, 0006, etc. (pares por convención)

## ⚙️ Contextos Obligatorios

### **Para esquema PUBLIC:**
```sql
-- changeset gymlog:NNNN-public-descripcion context:public
-- comment: Descripción del cambio en esquema public
```

### **Para esquemas TENANT:**
```sql
-- changeset gymlog:NNNN-tenant-descripcion context:tenant
-- comment: Descripción del cambio en esquemas de tenant
```

## 📝 Plantillas

### **Changeset PUBLIC:**
```sql
-- liquibase formatted sql

-- changeset gymlog:NNNN-public-descripcion context:public
-- comment: Descripción detallada del cambio

-- Tu SQL aquí para esquema public
CREATE TABLE IF NOT EXISTS public.nueva_tabla (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- campos...
);

-- rollback DROP TABLE IF EXISTS public.nueva_tabla;
```

### **Changeset TENANT:**
```sql
-- liquibase formatted sql

-- changeset gymlog:NNNN-tenant-descripcion context:tenant
-- comment: Descripción detallada del cambio

-- Tu SQL aquí para esquemas de tenant
CREATE TABLE nueva_tabla (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- campos...
);

-- rollback DROP TABLE IF EXISTS nueva_tabla;
```

## 🎯 Reglas Importantes

1. **✅ SIEMPRE** usar contextos (`context:public` o `context:tenant`)
2. **✅ SIEMPRE** incluir rollback
3. **✅ SIEMPRE** añadir al `db.changelog-master.xml` en orden
4. **✅ PUBLIC antes que TENANT** si hay dependencias
5. **✅ Nombres descriptivos** y funcionales
6. **✅ Comentarios claros** explicando el propósito

## 🔍 Verificación

Para verificar que los changesets se aplican correctamente:

1. **Esquema public**: Solo debe tener tabla `tenants` y configuraciones globales
2. **Esquemas tenant**: Deben tener todas las tablas de negocio (users, routines, exercises, etc.)
3. **Logs**: `TenantLiquibaseManager` registra la ejecución por esquema

## 📊 Estado Actual

- `0001-public-tenants-table.sql` ✅ - Tabla de tenants en public
- `0002-tenant-schema-initial.sql` ✅ - Esquema inicial de cada tenant