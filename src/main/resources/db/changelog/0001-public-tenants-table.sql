-- liquibase formatted sql

-- changeset gymlog:0001-public-tenants-table context:public
-- comment: Crear tabla de tenants en esquema public para arquitectura multi-tenant

CREATE TABLE IF NOT EXISTS public.tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    plan VARCHAR(50) NOT NULL CHECK (plan IN ('basic', 'premium', 'enterprise')),
    description TEXT,
    address TEXT,
    phone VARCHAR(50),
    schema_name VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_tenants_email ON public.tenants(email);
CREATE INDEX IF NOT EXISTS idx_tenants_schema_name ON public.tenants(schema_name);
CREATE INDEX IF NOT EXISTS idx_tenants_is_active ON public.tenants(is_active);

-- Insertar tenant por defecto para desarrollo
INSERT INTO public.tenants (id, name, email, plan, description, schema_name, is_active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Gimnasio Demo',
    'admin@demo.gymlog.com',
    'premium',
    'Gimnasio de demostración para desarrollo y testing',
    'gym_demo',
    true
) ON CONFLICT (id) DO NOTHING;

-- rollback DROP TABLE IF EXISTS public.tenants;